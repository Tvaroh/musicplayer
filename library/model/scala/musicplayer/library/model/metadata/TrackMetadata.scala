package musicplayer.library.model.metadata

import musicplayer.library.model._

case class TrackMetadata(artistName: Option[ArtistName],
                         albumArtistName: Option[ArtistName],
                         albumName: Option[AlbumName],
                         title: Option[TrackTitle],
                         number: Option[TrackNumber],
                         year: Option[TrackYear],
                         durationSeconds: Int)
