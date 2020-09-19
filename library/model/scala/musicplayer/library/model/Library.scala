package musicplayer.library.model

import scala.collection.immutable.SeqMap

case class Library(artists: SeqMap[ArtistName, Artist])

object Library {

  def apply(tracks: Seq[Track]): Library = {
    val artists =
      tracks.groupBy(_.metadata.artistName)
        .map { case (artistName, artistTracks) =>
          val artistAlbums =
            artistTracks.groupBy(_.metadata.albumName)
              .map { case (albumName, albumTracks) =>
                Album(
                  albumName,
                  SeqMap.from {
                    albumTracks.view
                      .sortBy(track => (track.metadata.number, track.metadata.name))
                      .map(track => (track.metadata.name, track))
                  }
                )
              }
              .toSeq

          Artist(
            artistName,
            SeqMap.from {
              artistAlbums.view
                .sortBy(album => (album.year, album.name))
                .map(album => (album.name, album))
            }
          )
        }
        .toSeq

    Library(
      SeqMap.from {
        artists.view
          .sortBy(_.name)
          .map(artist => (artist.name, artist))
      }
    )
  }

}
