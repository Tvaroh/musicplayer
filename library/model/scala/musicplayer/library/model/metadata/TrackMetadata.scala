package musicplayer.library.model.metadata

import musicplayer.library.model._

case class TrackMetadata(artistName: Option[ArtistName],
                         albumName: Option[AlbumName],
                         albumArtistName: Option[ArtistName],
                         title: Option[TrackTitle],
                         number: Option[TrackNumber],
                         year: Option[TrackYear])
