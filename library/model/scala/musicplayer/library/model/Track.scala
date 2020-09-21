package musicplayer.library.model

import java.nio.file.Path

case class Track(path: Path,
                 artistName: ArtistName,
                 albumName: Option[AlbumName],
                 albumArtistName: Option[ArtistName],
                 title: TrackTitle,
                 number: Option[TrackNumber],
                 year: Option[TrackYear])
