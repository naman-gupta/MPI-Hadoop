
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

// first iteration, k-random centers, in every follow-up iteration we have new calculated centers
@SuppressWarnings("deprecation")
public class KMeansMapper extends
    Mapper<LongWritable, Text, Text, Text> {

  private final List<String> centers = new ArrayList<String>();
  
  @Override
  protected void setup(Context context) throws IOException,
      InterruptedException {
    super.setup(context);
    Configuration conf = context.getConfiguration();
   // Path centroids = new Path("kmeans/center/centers.txt");
   // System.out.println(conf.get("centroid.path"));
    Path centroids = new Path(conf.get("centroid.path"));
    FileSystem fs = FileSystem.get(conf);
    
    BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(centroids)));
    String line = null;
    try
    {
    	while ((line= br.readLine()) != null)
    	{
            centers.add(line);
    	}
    }catch(Exception e){}
  }

  @Override
  protected void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {
  //	System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    String nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    for (String c : centers) {
      double dist = dist(c,value.toString());
      if (null==nearest) {
        nearest = c;
        nearestDistance = dist;
      } else {
        if (nearestDistance > dist) {
          nearest = c;
          nearestDistance = dist;
        }
      }
    }
    Text word = new Text();
    word.set(nearest);
   // System.out.println(word+" "+value);
    context.write(word, value);
  }
  
  private static double dist(String point, String center)
  {
	  double point_x = Double.parseDouble(point.split(",")[0]);
	  double point_y = Double.parseDouble(point.split(",")[1]);
	  
	  double center_x = Double.parseDouble(center.split(",")[0]);
	  double center_y = Double.parseDouble(center.split(",")[1]);
	 // System.out.println(Math.sqrt(Math.pow((center_x - point_x), 2) + Math.pow((center_y - point_y), 2)));
      return Math.sqrt(Math.pow((center_x - point_x), 2) + Math.pow((center_y - point_y), 2));
  }

}
