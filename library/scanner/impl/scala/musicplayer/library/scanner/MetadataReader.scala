package musicplayer.library.scanner

import java.nio.file.Path

import cats.effect.{Async, Blocker, ContextShift}
import musicplayer.library.model.metadata.TrackMetadata
import musicplayer.library.model._
import taggedtypes._
import uk.co.caprica.vlcj.factory.MediaApi
import uk.co.caprica.vlcj.media._

trait MetadataReader[F[_]] {

  def readMetadata(filePath: Path): F[Option[TrackMetadata]]

}

class MetadataReaderImpl[F[_]](mediaApi: MediaApi)
                              (implicit F: Async[F],
                                        blocker: Blocker,
                                        cs: ContextShift[F])
  extends MetadataReader[F] {

  import MetadataReaderImpl._

  override def readMetadata(filePath: Path): F[Option[TrackMetadata]] =
    blocker.blockOn {
      F.bracket(F.delay(mediaApi.newMedia(filePath.toString)))(readFileMetadata)(media => F.delay(media.release()))
    }

  private def readFileMetadata(media: Media) =
    F.async[Option[TrackMetadata]] { cb =>
      media.events().addMediaEventListener(new MediaEventAdapter {
        override def mediaParsedChanged(media: Media, status: MediaParsedStatus): Unit = {
          cb {
            Right {
              if (status == MediaParsedStatus.DONE) Some(toTrackMetadata(media.meta().asMetaData()))
              else None
            }
          }

          media.events().removeMediaEventListener(this)
        }
      })

      media.parsing().parse(ParseFlag.FETCH_LOCAL)

      ()
    }

}

private object MetadataReaderImpl {

  private def toTrackMetadata(metadata: MetaData): TrackMetadata = {

    def parseTrackNumber(number: String): Option[TrackNumber] =
      if (number.exists(!_.isDigit)) None else Some(TrackNumber(number.toInt))

    def parseYear(date: String): Option[TrackYear] = {
      val hyphenIndex = date.indexOf('-')

      val yearPart = if (hyphenIndex > 0) date.substring(0, hyphenIndex) else date

      if (yearPart.exists(!_.isDigit) || yearPart.length > 4) None else Some(TrackYear(yearPart.toInt))
    }

    TrackMetadata(
      Option(metadata.get(Meta.ARTIST)).map(_.trim) @@@ ArtistName,
      Option(metadata.get(Meta.ALBUM)).map(_.trim) @@@ AlbumName,
      Option(metadata.get(Meta.ALBUM_ARTIST)).map(_.trim) @@@ AlbumName,
      Option(metadata.get(Meta.TITLE)).map(_.trim) @@@ TrackTitle,
      Option(metadata.get(Meta.TRACK_NUMBER)).map(_.trim).flatMap(parseTrackNumber),
      Option(metadata.get(Meta.DATE)).map(_.trim).flatMap(parseYear)
    )
  }

}
