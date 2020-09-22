package musicplayer.library.model

import java.nio.file.Path

case class Track(path: Path,
                 artistName: Option[ArtistName],
                 albumArtistName: Option[ArtistName],
                 albumName: Option[AlbumName],
                 title: TrackTitle,
                 number: Option[TrackNumber],
                 year: Option[TrackYear],
                 durationSeconds: Int) {

  val effectiveArtistName: Option[ArtistName] =
    artistName.orElse(albumArtistName)

}

object Track {

  def apply(path: Path, metadata: TrackMetadata): Option[Track] =
    metadata.title.map {
      Track(
        path,
        metadata.artistName,
        metadata.albumArtistName,
        metadata.albumName,
        _,
        metadata.number,
        metadata.year,
        metadata.durationSeconds
      )
    }

}
