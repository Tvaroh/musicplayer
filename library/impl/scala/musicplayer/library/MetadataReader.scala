package musicplayer.library

import java.nio.file.Path

import cats.effect.{Blocker, ContextShift, Sync}
import musicplayer.library.model._
import musicplayer.library.model.metadata.TrackMetadata
import taggedtypes._
import uk.co.caprica.vlcjinfo.{MediaInfo, MediaInfoParseException}

import scala.util.control.Exception
import scala.util.control.Exception.catching

trait MetadataReader[F[_]] {

  def readMetadata(filePath: Path): F[Option[TrackMetadata]]

}

class MetadataReaderImpl[F[_]](implicit F: Sync[F],
                                        blocker: Blocker,
                                        cs: ContextShift[F])
  extends MetadataReader[F] {

  import MetadataReaderImpl._

  override def readMetadata(filePath: Path): F[Option[TrackMetadata]] =
    blocker.delay {
      for {
        mediaInfo <- mediaInfoParseCatch.opt(MediaInfo.mediaInfo(filePath.toString))
        generalInfo <- Option(mediaInfo.first("General"))
        audioInfo <- Option(mediaInfo.first("Audio"))
      } yield {
        TrackMetadata(
          Option(generalInfo.value("Performer")) @@@ ArtistName,
          Option(generalInfo.value("Album")) @@@ AlbumName,
          Option(generalInfo.value("Album/Performer")) @@@ ArtistName,
          Option(generalInfo.value("Track name")) @@@ TrackTitle,
          Option(generalInfo.value("Track name/Position")).flatMap(parseTrackNumber),
          Option(generalInfo.value("Recorded date")).flatMap(parseYear),
          parseDurationSeconds(audioInfo.value("Duration"))
        )
      }
    }

}

private object MetadataReaderImpl {

  private val mediaInfoParseCatch: Exception.Catch[MediaInfo] =
    catching(classOf[MediaInfoParseException])

  private def parseTrackNumber(number: String): Option[TrackNumber] =
    if (number.exists(!_.isDigit)) None else Some(TrackNumber(number.toInt))

  private def parseYear(date: String): Option[TrackYear] = {
    val hyphenIndex = date.indexOf('-')
    val yearPart = if (hyphenIndex > 0) date.substring(0, hyphenIndex) else date

    if (yearPart.exists(!_.isDigit) || yearPart.length > 4) None else Some(TrackYear(yearPart.toInt))
  }

  private def parseDurationSeconds(duration: String): Int =
    duration
      .split(' ')
      .sliding(2, 2)
      .foldLeft(0) { case (acc, Array(number, unit)) =>
        val seconds = unit match {
          case "s" => number.toInt
          case "min" => number.toInt * 60
          case "h" => number.toInt * 3600
          case _ => 0
        }

        acc + seconds
      }

}
