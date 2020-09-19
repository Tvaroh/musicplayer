package musicplayer.library.model

import scala.collection.immutable.SeqMap

case class Artist(name: ArtistName,
                  albums: SeqMap[AlbumName, Album],
                  uncategorizedTracks: SeqMap[TrackTitle, Track])
