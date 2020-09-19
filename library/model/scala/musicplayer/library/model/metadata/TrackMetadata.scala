package musicplayer.library.model.metadata

import musicplayer.library.model._

case class TrackMetadata(artistName: ArtistName,
                         albumName: AlbumName,
                         name: TrackName,
                         number: Option[TrackNumber],
                         year: Option[TrackYear])
