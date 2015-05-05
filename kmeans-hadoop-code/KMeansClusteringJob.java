

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

@SuppressWarnings("deprecation")
public class KMeansClusteringJob {

   public static void main(String[] args) throws IOException,
      InterruptedException, ClassNotFoundException {

    int iteration = 1;
    Configuration conf = new Configuration();
    conf.set("num.iteration", iteration + "");

    Path in = new Path("kmeans/data");
    Path center = new Path("kmeans/center/centers.txt");
    conf.set("centroid.path", center.toString());
    Path out = new Path("kmeans/depth_1");

    Job job = new Job(conf);
    job.setJobName("KMeans Clustering");

    job.setMapperClass(KMeansMapper.class);
    job.setReducerClass(KMeansReducer.class);
    job.setJarByClass(KMeansClusteringJob.class);

    
    FileSystem fs = FileSystem.get(conf);
    if (fs.exists(out)) {
      fs.delete(out, true);
    }
    FileInputFormat.addInputPath(job, in);
    FileOutputFormat.setOutputPath(job, out);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    job.waitForCompletion(true);

    long counter = job.getCounters()
        .findCounter(KMeansReducer.Counter.CONVERGED).getValue();
    iteration++;
    long time=0;
    while (counter > 0 && iteration < 10) {
      conf = new Configuration();
      conf.set("centroid.path", center.toString());
      conf.set("num.iteration", iteration + "");
      job = new Job(conf);
      job.setJobName("KMeans Clustering " + iteration);

      job.setMapperClass(KMeansMapper.class);
      job.setReducerClass(KMeansReducer.class);
      job.setJarByClass(KMeansMapper.class);

     
      //in = new Path("kmeans/depth_" + (iteration - 1) + "/");
      in = new Path("kmeans/data");
      
      out = new Path("kmeans/depth_" + iteration);

      FileInputFormat.addInputPath(job, in);
      if (fs.exists(out))
        fs.delete(out, true);

      FileOutputFormat.setOutputPath(job, out);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      long start = new Date().getTime();
      job.waitForCompletion(true);            
      long end = new Date().getTime();
      System.out.println("Job took "+(end-start) + "milliseconds");
      time =time+(end-start);
     // job.submit();
      //job.waitForCompletion(false);
      iteration++;
      counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED)
          .getValue();
    }
    
    System.out.println("Total time Taken :" + time);

 //   Path result = new Path("files/clustering/depth_" + (iteration - 1) + "/");
/*
    FileStatus[] stati = fs.listStatus(result);
    for (FileStatus status : stati) {
      if (!status.isDir()) {
        Path path = status.getPath();
        if (!path.getName().equals("_SUCCESS")) {
          
          try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
              conf)) {
            ClusterCenter key = new ClusterCenter();
            VectorWritable v = new VectorWritable();
            while (reader.next(key, v)) {
          
            }
          }
        }
      }
    }*/
  }

  public static void writeExampleVectors(Configuration conf, Path in,
      FileSystem fs) throws IOException {/*
    try (SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf,
        in, ClusterCenter.class, VectorWritable.class)) {
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(1, 2));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(16, 3));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(3, 3));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(2, 2));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(2, 3));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(25, 1));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(7, 6));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(6, 5));
      dataWriter.append(new ClusterCenter(new VectorWritable(0, 0)),
          new VectorWritable(-1, -23));
    }
  */}

  public static void writeExampleCenters(Configuration conf, Path center,
      FileSystem fs) throws IOException {/*
    try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf,
        center, ClusterCenter.class, IntWritable.class)) {
      final IntWritable value = new IntWritable(0);
      centerWriter.append(new ClusterCenter(new VectorWritable(1, 1)), value);
      centerWriter.append(new ClusterCenter(new VectorWritable(5, 5)), value);
    }
  */}

}
