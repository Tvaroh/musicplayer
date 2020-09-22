package musicplayer.library.model

import java.nio.file.Path

import cats.implicits._
import musicplayer.library.model.metadata.TrackMetadata

case class Track(path: Path,
                 artistName: ArtistName,
                 albumName: Option[AlbumName],
                 albumArtistName: Option[ArtistName],
                 title: TrackTitle,
                 number: Option[TrackNumber],
                 year: Option[TrackYear],
                 durationSeconds: Int)

object Track {

  def apply(path: Path, metadata: TrackMetadata): Option[Track] =
    (metadata.artistName, metadata.title).mapN { case (artistName, trackTitle) =>
      Track(
        path,
        artistName,
        metadata.albumName,
        metadata.albumArtistName,
        trackTitle,
        metadata.number,
        metadata.year,
        metadata.durationSeconds
      )
    }

}
