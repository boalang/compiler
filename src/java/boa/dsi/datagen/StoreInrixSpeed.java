package boa.dsi.datagen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.eclipse.wst.jsdt.internal.compiler.parser.Scanner;

import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSIProperties;
import boa.types.TransportationSchema.SpeedReading;
import boa.types.TransportationSchema.SpeedRoot;

public class StoreInrixSpeed {
	public static Configuration conf=new Configuration();
	public static SequenceFile.Reader seqFileReader;
	public static SequenceFile.Writer seqFileWriter;
	public static String staticFile = "/Users/mislam/Desktop/Data/metdata/static_File.txt";
	public static String gidLocation = "/Users/mislam/Desktop/Research/ResearchWeeklyProgress/29_March_2017/GidLocation.txt";
	public static HashMap<String, Location> detectorLoc =new HashMap<>();
	public static HashMap<String, Location> detectorEndLoc =new HashMap<>();
	public static HashMap<Integer, Location> gidLocMap =new HashMap<>();
	public static HashMap<String, String> detRoad =new HashMap<>();
	public static String inrixFile ="/Users/mislam/Desktop/Data/Speed/inrixdata.csv";
	public static void main(String[] args) throws IOException {
		int count =0;
		openWriter(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_SPEED_FILE_NAME);
		PrintWriter out = new PrintWriter(new FileWriter("log.txt"));
		fillDetectors(detectorLoc,detectorEndLoc,detRoad);
		fillGIDLocation(gidLocMap);
		//SpeedRoot.Builder sb =SpeedRoot.newBuilder();
		//Time.Builder tb = Time.newBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(inrixFile));
		
		
		long start = System.currentTimeMillis()/1000;
		System.out.println(start/60);
		//1 Month dat for inrix
		System.out.println("Started at: "+start);
		//System.out.println(reader.readLine());
		Scanner sc;
		boolean fileGenMode =true;
		//System.out.println(detectorLoc);
		int currentGrid =1;
		boolean ok =true;
		File directory = new File(inrixFile);
		File [] flist = new File[0];
		//System.out.println(flist.length);
		for (File file : flist) { 
			
			String name = file.getName().substring(0,file.getName().lastIndexOf('.'));
			if(name.length()==0)
			{
				continue;
			}
			BufferedReader freader =new BufferedReader(new FileReader(file));
			int gridid = Integer.parseInt(name);
			System.out.println("Data for GID: "+gridid);
			SpeedRoot.Builder sb = SpeedRoot.newBuilder();
			while(true)
			{
				String line = freader.readLine();
				if(line == null || line.isEmpty())
				{
					break;
				}
				String [] tokens = line.split(",");
				Location loc = detectorLoc.get(tokens[0]);
				
				
				if(loc==null)
				{
					System.out.println(loc);
					continue;
				}
				
				double lat = loc.lat;
				double lon =loc.lon;
				int gidRow =(int) ((lat  - 40.37)/.01)*660;
				int row = gidRow/660;
				int gidCol = (int) (1+ (lon + 96.70)/.01);
				int gid = row*660 + gidCol;
				String detid = tokens[0];
				String time  = tokens[8];
				int cvalue = 0;
				String roadname = detRoad.get(detid);
				int speed= 0;
				int avg =0;
				try {
					speed =Integer.parseInt(tokens[4]);
					//System.out.println(speed);
				} catch (Exception e) {
					System.out.println("Speed not found for line: ");
					System.out.println(line);
				}
				
				try {
					avg =Integer.parseInt(tokens[5]);
				} catch (Exception e) {
					System.out.println("Speed not found for line: ");
					System.out.println(line);
				}
				
				long ltime= getTimeStamp(time);
				boa.types.TransportationSchema.Location.Builder lb = boa.types.TransportationSchema.Location.newBuilder();
				lb.setLat(loc.lat);
				lb.setLon(loc.lon);
				SpeedReading.Builder sr = SpeedReading.newBuilder();
				sr.setGid(gid);
				sr.setCode(detid);
				sr.setTime(ltime);
				sr.setLatlon(lb.build());
				sr.setType("NA");
				sr.setSpeed(speed);
				sr.setAverage(speed);
				sr.setReference(speed);
				sr.setScore(30);
				sr.setCvalue(cvalue);
				sr.setRoadname(roadname);
				sb.addSpeeds(sr.build());
			}
			GeneratedMessage data = sb.build();
			String key = "!!"+gridid+"!!";
			seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
			freader.close();
			//System.out.println("Name: "+name+" "+(name.length()==0));
		}
		
		HashMap<Integer, ArrayList<String>> keymap =new HashMap<Integer, ArrayList<String>>();
		while(true){
			String line =reader.readLine();
			if(line == null || line.isEmpty())
			{
				break;
			}
			String [] tokens = line.split(",");
			System.out.println("Putting data to hashmap for detid: "+tokens[0]);
			Location loc = detectorLoc.get(tokens[0]);
			
			
			if(loc==null)
			{
				System.out.println(loc);
				continue;
			}
			double lat = loc.lat;
			double lon =loc.lon;
			int gidRow =(int) ((lat  - 40.37)/.01)*660;
			int row = gidRow/660;
			int gidCol = (int) (1+ (lon + 96.70)/.01);
			int gid = row*660 + gidCol;
			if(keymap.containsKey(gid)){
				ArrayList<String> list = keymap.get(gid);
				list.add(line);
				keymap.put(gid, list);
			}
			else
			{
				ArrayList<String> list =new ArrayList<String>();
				list.add(line);
				keymap.put(gid, list);
			}
		}
		
		for(int detid : keymap.keySet()){
			
			ArrayList<String> list = keymap.get(detid);
			System.out.println("Data for GID: "+detid);
			SpeedRoot.Builder sb = SpeedRoot.newBuilder();
			for (String line : list) {
				String [] tokens = line.split(",");
//				Location loc = detectorLoc.get(tokens[0]);
//				
//				
//				if(loc==null)
//				{
//					System.out.println(loc);
//					continue;
//				}
//				
//				double lat = loc.lat;
//				double lon =loc.lon;
//				int gidRow =(int) ((lat  - 40.37)/.01)*660;
//				int row = gidRow/660;
//				int gidCol = (int) (1+ (lon + 96.70)/.01);
//				int gid = row*660 + gidCol;
//				String detid = tokens[0];
				Location loc = detectorLoc.get(tokens[0]);
				
				
				if(loc ==null){
					System.out.println(loc);
					continue;
				}
				double lat = loc.lat;
				double lon =loc.lon;
				int gidRow =(int) ((lat  - 40.37)/.01)*660;
				int row = gidRow/660;
				int gidCol = (int) (1+ (lon + 96.70)/.01);
				int gid = row*660 + gidCol;
				String time  = tokens[8];
				int cvalue = 0;
				String roadname = detRoad.get(tokens[0]);
				System.out.println(roadname);
				
				int speed= 0;
				int avg =0;
				try {
					speed =Integer.parseInt(tokens[4]);
					//System.out.println(speed);
				} catch (Exception e) {
					System.out.println("Speed not found for line: ");
					System.out.println(line);
				}
				
				try {
					avg =Integer.parseInt(tokens[5]);
				} catch (Exception e) {
					System.out.println("Speed not found for line: ");
					System.out.println(line);
				}
				
				long ltime= getTimeStamp(time);
				boa.types.TransportationSchema.Location.Builder lb = boa.types.TransportationSchema.Location.newBuilder();
				lb.setLat(loc.lat);
				lb.setLon(loc.lon);
				SpeedReading.Builder sr = SpeedReading.newBuilder();
				sr.setGid(gid);
				sr.setCode(tokens[0]);
				sr.setTime(ltime);
				sr.setLatlon(lb.build());
				sr.setType("NA");
				sr.setSpeed(speed);
				sr.setAverage(speed);
				sr.setReference(speed);
				sr.setScore(30);
				sr.setCvalue(cvalue);
				sr.setRoadname(roadname);
				sb.addSpeeds(sr.build());
			}
			
			GeneratedMessage data = sb.build();
			String key = "!!"+detid+"!!";
			seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
			System.out.println("Data writtend for "+detid);
		}
		while(!ok)
		{
			String line =reader.readLine();
			if(line == null || line.isEmpty())
			{
				break;
			}
			count++;
			
			String [] tokens = line.split(",");
			Location loc = detectorLoc.get(tokens[0]);
			
			
			if(loc==null)
			{
				System.out.println(loc);
				continue;
			}
			
			double lat = loc.lat;
			double lon =loc.lon;
			int gidRow =(int) ((lat  - 40.37)/.01)*660;
			int row = gidRow/660;
			int gidCol = (int) (1+ (lon + 96.70)/.01);
			int gid = row*660 + gidCol;
			
			if(fileGenMode == true)
			{
				//System.out.println("Writing");
//				FileWriter fw = new FileWriter("/Users/mislam/Desktop/Data/Speed/datagen/"+gid+".txt",true);
//				BufferedWriter bw = new BufferedWriter(fw);
//				PrintWriter pw =new PrintWriter(bw);
//				pw.println(line);
//				pw.close();
//				bw.close();
//				fw.close();
				continue;
			}
			
		
			
			String detid = tokens[0];
			String time  = tokens[2];
			String [] times = time.split("T");
			String [] ymd= times[0].split("-");
			String [] hms = times[1].split(":");
			int year = Integer.parseInt(ymd[0]);
			int month = Integer.parseInt(ymd[1]);
			int day = Integer.parseInt(ymd[2]);
			int hour = Integer.parseInt(hms[0]);
			int minute = Integer.parseInt(hms[1]);
			int second = Integer.parseInt(hms[2]);
			String score = tokens[3];
			int cvalue =Integer.parseInt(tokens[1]);
			String roadname = detRoad.get(detid);
			int speed= 0;
			int avg =0;
			try {
				speed =Integer.parseInt(tokens[4]);
			} catch (Exception e) {
				System.out.println("Speed not found for line: ");
				System.out.println(line);
			}
			
			try {
				avg =Integer.parseInt(tokens[5]);
			} catch (Exception e) {
				System.out.println("Speed not found for line: ");
				System.out.println(line);
			}
			
			SpeedRoot.Builder sb = SpeedRoot.newBuilder();
			long ltime= getTimeStamp(time);
			sb.setTime(ltime);
			boa.types.TransportationSchema.Location.Builder lb = boa.types.TransportationSchema.Location.newBuilder();
			lb.setLat(loc.lat);
			lb.setLon(loc.lon);
			sb.setLatlon(lb.build());
			SpeedReading.Builder sr = SpeedReading.newBuilder();
			sr.setGid(gid);
			sr.setCode(detid);
			sr.setTime(ltime);
			sr.setLatlon(lb.build());
			sr.setType("NA");
			sr.setSpeed(speed);
			sr.setAverage(speed);
			sr.setReference(speed);
			sr.setScore(30);
			sr.setCvalue(cvalue);
			sr.setRoadname(roadname);
			sb.addSpeeds(sr.build());
			
			GeneratedMessage data = sb.build();
			String key = gid+"!!";
			seqFileWriter.append(new Text(key), new BytesWritable(data.toByteArray()));
			
		}
		long end = System.currentTimeMillis()/1000;
		System.out.println(end/60);
		System.out.println("Ended at: "+end);
		//reader.close();
		
		closeWrite();
	}
	
	public static void fillGIDLocation(HashMap<Integer, Location> gidLocMap)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(gidLocation));
			while(true){
				String line =reader.readLine();
				if(line ==null || line.isEmpty())
				{
					break;
				}
				String [] tokens = line.split(",");
				int gid = Integer.parseInt(tokens[0]);
				double lat = Double.parseDouble(tokens[1]);
				double lon = Double.parseDouble(tokens[2]);
				Location loc = new Location(lat, lon);
				gidLocMap.put(gid,loc);
				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	
	public static void fillDetectors(HashMap<String, Location> map,HashMap<String, Location> map2,HashMap<String, String> detRoad)
	{
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(staticFile));
			reader.readLine();
			int count = 0;
			while(true)
			{
				String line =reader.readLine();
				if(line ==null || line.isEmpty())
				{
					break;
				}
				String [] tokens = line.split("\t");
				double lat = Double.parseDouble(tokens[4]);
				double lon = Double.parseDouble(tokens[5]);
				Location loc =new Location(lat, lon);
				map.put(tokens[0], loc);
				lat = Double.parseDouble(tokens[6]);
				lon = Double.parseDouble(tokens[7]);
				loc =new Location(lat, lon);
				map2.put(tokens[0], loc);
				detRoad.put(tokens[0],tokens[2]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter("inrixDetectorLocations.csv"));
			out.println("Detector ID, Start Latitude,Start Longitude, End Latitude,End Longitude,Road Name");
			for (String s : map.keySet()) {
				out.println(s+", "+map.get(s).lat+", "+map.get(s).lon+", "+map2.get(s).lat+", "+map2.get(s).lon+" , "+detRoad.get(s));
			}
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	// Finding distance between two latitude  longitude locations
	
	public static double distance(double lat1, double lat2, double lon1,
	        double lon2, double el1, double el2) {

	    final int R = 6371; // Radius of the earth

	    Double latDistance = Math.toRadians(lat2 - lat1);
	    Double lonDistance = Math.toRadians(lon2 - lon1);
	    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c * 1000; // convert to meters

	    double height = el1 - el2;

	    distance = Math.pow(distance, 2) + Math.pow(height, 2);

	    return Math.sqrt(distance);
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
//			this.seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
//					BytesWritable.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	private static class Location{
		public double lat;
		public double lon;
		public Location(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		
		@Override
		public String toString() {
			return "Location: ("+lat+", "+lon+")";
		}
		
	}
}
