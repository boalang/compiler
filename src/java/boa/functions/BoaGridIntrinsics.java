package boa.functions;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.types.TransportationSchema.Grid;
import boa.types.TransportationSchema.SpeedRoot;
import boa.types.TransportationSchema.WeatherRoot;




public class BoaGridIntrinsics {

	private static Context context;
	private static MapFile.Reader  weatherMap, speedMap;
	private static final WeatherRoot emptyWeatherRoot = WeatherRoot.newBuilder().build();
	private static final SpeedRoot emptySpeed = SpeedRoot.newBuilder().build();
	
	
	@FunctionSpec(name = "equals", returnType = "bool", formalParameters = { "string", "string" })
	public static boolean equals(String a, String b)
	{
		return a.equals(b);
	}
	@FunctionSpec(name = "getweather", returnType = "WeatherRoot", formalParameters = { "Grid" })
	public static WeatherRoot getWeather(Grid g)
	{
		String key = g.getGidlocation().getLat()+"!!"+g.getGidlocation().getLon();
		key = "!!"+g.getId()+"!!";
		if(weatherMap == null)
		{
			openWeatherMap();
		}
		
		try {
			final BytesWritable value = new BytesWritable();
			if (weatherMap.get(new Text(key), value) == null) {
				System.out.println("Weather Data Not Found");
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final WeatherRoot root = WeatherRoot.parseFrom(_stream);
				System.out.println("Weather Data Found");
			 return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}

		System.err.println("error with weather: " + key);
		return emptyWeatherRoot;
	}
	
	@FunctionSpec(name = "getweather", returnType = "WeatherRoot", formalParameters = { "int" })
	public static WeatherRoot getWeather(long gid)
	{
		String key = "!!"+gid+"!!";
		if(weatherMap == null)
		{
			openWeatherMap();
		}
		
		try {
			final BytesWritable value = new BytesWritable();
			if (weatherMap.get(new Text(key), value) == null) {
				System.out.println("Weather Data Not Found");
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final WeatherRoot root = WeatherRoot.parseFrom(_stream);
			 return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}

		System.err.println("error with weather: " + key);
		return emptyWeatherRoot;
	}
	
	
	
	@FunctionSpec(name = "getspeed", returnType = "SpeedRoot", formalParameters = { "Grid" })
	public static SpeedRoot getSpeed(Grid g)
	{
		
		String key = g.getGidlocation().getLat()+"!!"+g.getGidlocation().getLon();
		key = "!!"+g.getId()+"!!";
		System.out.println(key);
		if(speedMap == null)
		{
			openSpeedMap();
		}
		
		try {
			final BytesWritable value = new BytesWritable();
			if (speedMap.get(new Text(key), value) == null) {
				System.out.println("Speed Data Not Found");
			} else {
				System.out.println("Speed Data Found");
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final SpeedRoot root = SpeedRoot.parseFrom(_stream);				
			 return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.err.println("error with speed: " + key);
		return emptySpeed;
	}
	
	@FunctionSpec(name = "getspeed", returnType = "SpeedRoot", formalParameters = { "int" })
	public static SpeedRoot getSpeed(long gid)
	{
		
		String key = gid+"";
		key = "!!"+gid+"!!";
		System.out.println(key);
		if(speedMap == null)
		{
			openSpeedMap();
		}
		
		try {
			final BytesWritable value = new BytesWritable();
			if (speedMap.get(new Text(key), value) == null) {
				System.out.println("Speed Data Not Found");
			} else {
				System.out.println("Speed Data Found");
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final SpeedRoot root = SpeedRoot.parseFrom(_stream);				
			 return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.err.println("error with speed: " + key);
		return emptySpeed;
	}
	
	
	private static void openSpeedMap() {
		final Configuration conf = new Configuration();
		try {
			final FileSystem fs = FileSystem.get(conf);
			String pathloc= "/Users/mislam/Desktop/datascienceInf/transData/speed.seq";
			final Path p = new Path(pathloc);
			speedMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void openWeatherMap() {
		final Configuration conf = new Configuration();
		try {
			final FileSystem fs = FileSystem.get(conf);
			String pathloc= "/Users/mislam/Desktop/datascienceInf/transData/weather.seq";
			final Path p = new Path(pathloc);
			weatherMap = new MapFile.Reader(fs, p.toString(), conf);
			//final BytesWritable value = new BytesWritable();
			//weatherMap.get(new Text("32!!"),value);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}