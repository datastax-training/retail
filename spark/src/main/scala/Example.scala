import com.datastax.spark.connector._
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
import java.util.UUID

case class Track(artist: String,
                 track: String,
                 track_id: UUID,
                 genre: String,
                 music_file: String,
                 starred:Option[Boolean],
                 track_length_in_seconds: Int)

object Example {

  def main(args: Array[String]) {

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val sc = new SparkContext("spark://127.0.0.1:7077", "test", conf)

    val tracks = sc.cassandraTable("playlist","track_by_artist").as(Track)

    tracks.map( track => (track.artist, 1) ).reduceByKey( _+_ ).saveToCassandra("playlist","cnt_of_tracks_by_artist")

  }
}
