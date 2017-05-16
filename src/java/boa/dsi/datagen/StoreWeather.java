package boa.dsi.datagen;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.parse.ANTLRParser.range_return;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.eclipse.wst.jsdt.internal.compiler.parser.Scanner;
import org.json.JSONObject;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.dsi.DSIProperties;
import boa.types.TransportationSchema.Location;
import boa.types.TransportationSchema.WeatherRecord;
//import boa.types.TransportationSchema2.County;
//import boa.types.TransportationSchema2.Grid;
//import boa.types.TransportationSchema2.Lane;
//import boa.types.TransportationSchema2.Location;
//import boa.types.TransportationSchema2.SpeedReading;
//import boa.types.TransportationSchema2.SpeedRoot;
//import boa.types.TransportationSchema2.Time;
//import boa.types.TransportationSchema2.VehicleClass;
//import boa.types.TransportationSchema2.WeatherRecord;
//import boa.types.TransportationSchema2.WeatherRoot;
import boa.types.TransportationSchema.WeatherRoot;

public class StoreWeather {
	public static Configuration conf=new Configuration();
	public static SequenceFile.Reader seqFileReader;
	public static SequenceFile.Writer seqFileWriter;
	public static String fileLoc="/Users/mislam/Desktop/Data/metdata";
	public static String detectorFile = "/Users/mislam/Desktop/Data/metdata/Detector.txt";
	public static String gidLoc="/Users/mislam/Desktop/Data/metdata/GidCountyLocation.txt";
	public static void main(String[] args) throws IOException {
		openWriter(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_WEATHER_FILE_NAME);
		HashMap<Integer, GidLatLon> gidLoc = new HashMap<>();
		fillDetector(gidLoc);
		System.out.println(gidLoc);
		ArrayList<String> files= getFiles();
		int count =0;
		long start =System.currentTimeMillis();
		System.out.println("Started at: "+start);
		boolean dataGenMode =true;
		BufferedReader bf;
		File directory =new File(fileLoc);
		File [] filesa =new File[0];
		int fileCount =0;
		for (File file : filesa) {
			if(dataGenMode)
			{
				break;
			}
			String name = file.getName().substring(0,file.getName().lastIndexOf('.'));
			System.out.println(name);
			if(name.length()==0)
			{
				continue;
			}
			bf = new BufferedReader(new FileReader(file));
			WeatherRoot.Builder wb =WeatherRoot.newBuilder();
			int gridid = Integer.parseInt(name);
			
			while(true)
			{
				String line = bf.readLine();
				if(line ==null || line.isEmpty() )
				{
					break;
				}
				String timestr = line.substring(line.lastIndexOf(',')+1);
				line =line.substring(0,line.lastIndexOf(','));
				JSONObject json;
				try {
					json= new JSONObject(line);
				} catch (Exception e) {
					continue;
				}
				
				name= line.substring(line.lastIndexOf('/')+1,line.lastIndexOf('.'));
				//System.out.println(name);
				String y= name.substring(0, 4);
				String m=name.substring(4,6);
				String d=name.substring(6,8);
				String hh=name.substring(8, 10);
				String mm=name.substring(10,12);
				int gid = Integer.parseInt(json.get("gid").toString());
				long ltime = Long.parseLong(timestr);
				GidLatLon latlon=gidLoc.get(Integer.parseInt(json.get("gid").toString()));
				Location.Builder lb = Location.newBuilder();
				if(latlon == null)
				{
					int row = gid/660;
					int col =gid%660;
					double lon = -96.70 +(col -row)*.01;
					double lat = 40.37+ row *.01;
					latlon = new GidLatLon(lat, lon);
				}
				lb.setLat(latlon==null?0:latlon.lat);
				lb.setLon(latlon==null?0:latlon.lon);
				WeatherRecord.Builder wr=WeatherRecord.newBuilder();
				wr.setGid(Integer.parseInt(json.get("gid").toString()));
				wr.setTmpc(Float.parseFloat(json.get("tmpc").toString()));
				wr.setWawa(0f);
				wr.setPtype(Float.parseFloat(json.get("ptype").toString()));
				wr.setDwpc(Float.parseFloat(json.get("dwpc").toString()));
				wr.setSmps(Float.parseFloat(json.get("smps").toString()));
				wr.setDrct(Float.parseFloat(json.get("drct").toString()));
				wr.setVsby(Float.parseFloat(json.get("vsby").toString()));
				wr.setRoadtmpc(Float.parseFloat(json.get("roadtmpc").toString()));
				wr.setSrad(Float.parseFloat(json.get("srad").toString()));
				wr.setSnwd(Float.parseFloat(json.get("snwd").toString()));
				wr.setPcpn(Float.parseFloat(json.get("pcpn").toString()));
				wr.setTime(ltime);
				wr.setLatlon(lb.build());
				wb.addWeather(wr.build());
			}
			
			GeneratedMessage data =wb.build();

			String key = "!!"+gridid+"!!";
			seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
			System.out.println("processed total files: "+fileCount++);
		}
		System.out.println(files.size());
		HashMap<Integer, ArrayList<String>> keymap =new HashMap<Integer, ArrayList<String>>();
		int fc =0;
		for (String string : files) {
			if(!dataGenMode)
			{
				break;
			}
			String l = string.substring(string.lastIndexOf('/')+1,string.lastIndexOf('.'));
			System.out.println(l);
			System.out.println("Out of "+files.size()+ " Total processed files is"+fc);
			fc++;
			java.util.Scanner sc = new java.util.Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(string)))));
			if(dataGenMode)
			{
				//break;
			}
			sc.nextLine();
			
			while( sc.hasNextLine())
			{
				String line =sc.nextLine();
				if(line ==null || line.isEmpty())
				{
					break;
				}
				//System.out.println(line);
				
				try
				{
					//System.out.println(line);
					JSONObject json = new JSONObject(line);
					
					
						//System.out.println(json.toString());
						WeatherRoot.Builder wb =WeatherRoot.newBuilder();
						String name= string.substring(string.lastIndexOf('/')+1,string.lastIndexOf('.'));
						//System.out.println(name);
						String y= name.substring(0, 4);
						String m=name.substring(4,6);
						String d=name.substring(6,8);
						String hh=name.substring(8, 10);
						String mm=name.substring(10,12);
						int gid = Integer.parseInt(json.get("gid").toString());
						if(dataGenMode == true)
						{
//							FileWriter fw = new FileWriter("/Users/mislam/Desktop/Speeds/wdata/"+gid+".txt",true);
//							System.out.println("Writing to file"+gid);
//							BufferedWriter bw = new BufferedWriter(fw);
//							PrintWriter pw =new PrintWriter(bw);
//							long ltime = getTimeStamp(y+"-"+m+"-"+d+"T"+hh+":"+mm+":00");
//							pw.println(line+ltime);
//							pw.close();
//							bw.close();
//							fw.close();
							
							if(keymap.containsKey(gid)){
								long ltime = getTimeStamp(y+"-"+m+"-"+d+"T"+hh+":"+mm+":00");
								ArrayList<String> list =keymap.get(gid);
								list.add(line+ltime);
								keymap.put(gid, list);
							}
							else
							{
								ArrayList<String> list = new ArrayList<String>();
								long ltime = getTimeStamp(y+"-"+m+"-"+d+"T"+hh+":"+mm+":00");
								list.add(line+ltime);
								System.out.println(line+ltime);
								keymap.put(gid, list);
							}
							continue;
						}
						//String ss=name.substring(12,14);
						//System.out.println(name+" : "+ss);
//						Time.Builder tb=Time.newBuilder();
//						tb.setDay(Integer.parseInt(d));
//						tb.setMonth(Integer.parseInt(m));
//						tb.setYear(Integer.parseInt(y));
//						tb.setSecond(0);
//						tb.setMinute(Integer.parseInt(mm));
//						tb.setHour(Integer.parseInt(hh));
						long ltime = getTimeStamp(y+"-"+m+"-"+d+"T"+hh+":"+mm+":00"); //TODO logic Correction
					//	System.out.println(ltime);
						wb.setTime(ltime);
						GidLatLon latlon=gidLoc.get(Integer.parseInt(json.get("gid").toString()));
						Location.Builder lb = Location.newBuilder();
						lb.setLat(latlon==null?0:latlon.lat);
						lb.setLon(latlon==null?0:latlon.lon);
						wb.setLatlon(lb.build());
						WeatherRecord.Builder wr=WeatherRecord.newBuilder();
						wr.setGid(Integer.parseInt(json.get("gid").toString()));
						wr.setTmpc(Float.parseFloat(json.get("tmpc").toString()));
						wr.setWawa(0f);
						wr.setPtype(Float.parseFloat(json.get("ptype").toString()));
						wr.setDwpc(Float.parseFloat(json.get("dwpc").toString()));
						wr.setSmps(Float.parseFloat(json.get("smps").toString()));
						wr.setDrct(Float.parseFloat(json.get("drct").toString()));
						wr.setVsby(Float.parseFloat(json.get("vsby").toString()));
						wr.setRoadtmpc(Float.parseFloat(json.get("roadtmpc").toString()));
						wr.setSrad(Float.parseFloat(json.get("srad").toString()));
						wr.setSnwd(Float.parseFloat(json.get("snwd").toString()));
						wr.setPcpn(Float.parseFloat(json.get("pcpn").toString()));
						wr.setTime(ltime);
						wr.setLatlon(lb.build());
						wb.addWeather(wr.build());
						GeneratedMessage data =wb.build();
//						String key = WeatherRoot.parseFrom(data.toByteArray()).getLatlon().getLat()+"!!"+
//								WeatherRoot.parseFrom(data.toByteArray()).getLatlon().getLon();
//						seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
						String key = json.get("gid").toString()+"!!";
						seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
					//}
					
				}
				catch(Exception e)
				{
					continue;
				}
//				GeneratedMessage data =sb.build();
//				String key = SpeedRoot.parseFrom(data.toByteArray()).getLatlon().getLat()+"!!"+
//						SpeedRoot.parseFrom(data.toByteArray()).getLatlon().getLon()
//						;
//				seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
			}
			sc.close();
		}
		
		for (int gid  : keymap.keySet()) {
			System.out.println("Writing data for gid: "+gid);
			ArrayList<String> lines =keymap.get(gid);
			WeatherRoot.Builder wb =WeatherRoot.newBuilder();
			int gridid = gid;
			
			for(String line : lines)
			{
				if(line ==null || line.isEmpty() )
				{
					break;
				}
				String timestr = line.substring(line.lastIndexOf(',')+1);
				line =line.substring(0,line.lastIndexOf(','));
				JSONObject json;
				try {
					json= new JSONObject(line);
				} catch (Exception e) {
					continue;
				}
				
				String name= line.substring(line.lastIndexOf('/')+1,line.lastIndexOf('.'));
				//System.out.println(name);
				String y= name.substring(0, 4);
				String m=name.substring(4,6);
				String d=name.substring(6,8);
				String hh=name.substring(8, 10);
				String mm=name.substring(10,12);
				//int gid = Integer.parseInt(json.get("gid").toString());
				long ltime = Long.parseLong(timestr);
				GidLatLon latlon=gidLoc.get(Integer.parseInt(json.get("gid").toString()));
				Location.Builder lb = Location.newBuilder();
				if(latlon == null)
				{
					int row = gid/660;
					int col =gid%660;
					double lon = -96.70 +(col -row)*.01;
					double lat = 40.37+ row *.01;
					latlon = new GidLatLon(lat, lon);
				}
				lb.setLat(latlon==null?0:latlon.lat);
				lb.setLon(latlon==null?0:latlon.lon);
				WeatherRecord.Builder wr=WeatherRecord.newBuilder();
				wr.setGid(Integer.parseInt(json.get("gid").toString()));
				wr.setTmpc(Float.parseFloat(json.get("tmpc").toString()));
				wr.setWawa(0f);
				wr.setPtype(Float.parseFloat(json.get("ptype").toString()));
				wr.setDwpc(Float.parseFloat(json.get("dwpc").toString()));
				wr.setSmps(Float.parseFloat(json.get("smps").toString()));
				wr.setDrct(Float.parseFloat(json.get("drct").toString()));
				wr.setVsby(Float.parseFloat(json.get("vsby").toString()));
				wr.setRoadtmpc(Float.parseFloat(json.get("roadtmpc").toString()));
				wr.setSrad(Float.parseFloat(json.get("srad").toString()));
				wr.setSnwd(Float.parseFloat(json.get("snwd").toString()));
				wr.setPcpn(Float.parseFloat(json.get("pcpn").toString()));
				wr.setTime(ltime);
				wr.setLatlon(lb.build());
				wb.addWeather(wr.build());
			}
			
			GeneratedMessage data =wb.build();

			String key = "!!"+gridid+"!!";
			seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
		}
		
		closeWrite();
		long end =System.currentTimeMillis();
		System.out.println("Ended at: "+end);
	}
	
	private static long getTimeStamp(String time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			Date date = df.parse(time);
			return date.getTime() * 1000000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static ArrayList<String> getFiles()
	{
		
		java.nio.file.Path path=java.nio.file.Paths.get("/Users/mislam/Desktop/Data/Weather/08");
		GetFilesInTheDirectory gf=new GetFilesInTheDirectory();
		List<String> list=gf.getAllIndividuals(path);
		System.out.println(list.size());
		return (ArrayList<String>) list;
	}
	
	public static void fillDetector(HashMap<Integer, GidLatLon> gidLoc2 ){
		try {
			java.util.Scanner sc= new java.util.Scanner(new File(gidLoc));
			sc.nextLine();
			while(sc.hasNextLine())
			{
				String line =sc.nextLine();
				//System.out.println(line);
				String [] names= line.split(",");
				if(names.length<3)
				{
					continue;
				}
				float lat =Float.parseFloat(names[2]);
				float lon = Float.parseFloat(names[3]);
				//System.out.println(lat+" "+lon);
				gidLoc2.put(Integer.parseInt(names[0]),new GidLatLon(lat, lon));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static boolean closeWrite() {
		try {
			seqFileWriter.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	public static boolean openWriter(String seqPath) {
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}

class GidLatLon
{
	double lat;
	double lon;
	public GidLatLon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
}

class GetFilesInTheDirectory extends SimpleFileVisitor<java.nio.file.Path> {
@Override
   public FileVisitResult postVisitDirectory(java.nio.file.Path dir,
                                         IOException exc) {
       System.out.format("Directory: %s%n", dir);
       return FileVisitResult.CONTINUE;
   }


public List<String> getAllIndividuals(java.nio.file.Path path) {
    final List<String> list = new ArrayList<>();

    try {
        Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>()
        {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isDirectory()) {
                    return FileVisitResult.CONTINUE;
                }
                String id = file.toFile().toString();
                if(id.endsWith("json"))
                {
               	list.add(id); 
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    } catch (IOException e) {
        System.out.println("Error getting all individuals");
    }

    return list;
}

}
