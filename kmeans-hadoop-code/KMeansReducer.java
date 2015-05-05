

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

// calculate a new clustercenter for these vertices
@SuppressWarnings("deprecation")
public class KMeansReducer extends
    Reducer<Text, Text, Text, Text> {

  public static enum Counter {
    CONVERGED
  }

  private final List<String> centers = new ArrayList<String>();
  int cluster = 0;

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context)throws IOException, InterruptedException {

    Double newCentroid_X = 0.0,newCentroid_Y = 0.0;
    int count =0;
   
    //System.out.println("***************************************");
    for (Text value : values) {

    	newCentroid_X += Double.parseDouble(value.toString().split(",")[0]);
    	newCentroid_Y += Double.parseDouble(value.toString().split(",")[1]);
    	count++;
    	Text t = new Text();
    	t.set(value.toString().split(",")[0]+","+value.toString().split(",")[1]);
    	
    	Text t1= new Text();
    	t1.set(cluster+"");
    	//context.write(t,t1);
    }
    cluster++;
    newCentroid_X = newCentroid_X/count;
    newCentroid_Y = newCentroid_Y/count;
    String newCentroid = newCentroid_X+","+newCentroid_Y;
    centers.add(newCentroid);
    
    if(!checkConvergence(key.toString(),newCentroid))
    	context.getCounter(Counter.CONVERGED).increment(1);

  }

  private static boolean checkConvergence(String point,String center)
  {
	
		  double point_x = Double.parseDouble(point.split(",")[0]);
		  double point_y = Double.parseDouble(point.split(",")[1]);
		  
		  double center_x = Double.parseDouble(center.split(",")[0]);
		  double center_y = Double.parseDouble(center.split(",")[1]);
		  //System.out.println(Math.sqrt(Math.pow((center_x - point_x), 2) + Math.pow((center_y - point_y), 2)));
	      return Math.sqrt(Math.pow((center_x - point_x), 2) + Math.pow((center_y - point_y), 2)) < 0.2;
  }
  @Override
  protected void cleanup(Context context) throws IOException,
      InterruptedException {
    super.cleanup(context);
    Configuration conf = context.getConfiguration();
    Path centroids = new Path(conf.get("centroid.path"));
    FileSystem fs = FileSystem.get(conf);
    fs.delete(centroids, true);
    BufferedWriter br=new BufferedWriter(new OutputStreamWriter(fs.create(centroids)));
    for(String center : centers)
    	br.write(center+"\n");
    br.flush();
    br.close();
   }
}
