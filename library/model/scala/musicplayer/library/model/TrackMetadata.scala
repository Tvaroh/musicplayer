package musicplayer.library.model

case class TrackMetadata(artistName: Option[ArtistName],
                         albumArtistName: Option[ArtistName],
                         albumName: Option[AlbumName],
                         title: Option[TrackTitle],
                         number: Option[TrackNumber],
                         year: Option[TrackYear],
                         durationSeconds: Int)
