package musicplayer.library.model

import java.nio.file.Path

import cats.implicits._

import scala.collection.immutable.SeqMap

case class LibraryMetadata(artists: SeqMap[ArtistName, Artist],
                           incompleteMetadataTracks: Seq[(Path, TrackMetadata)]) {

  val tracks: SeqMap[(ArtistName, AlbumName, TrackTitle), Track] =
    SeqMap.from {
      artists.flatMap { case (_, artist) =>
        artist.albums.flatMap { case (_, album) =>
          album.tracks.map { case (_, track) =>
            (artist.name, album.name, track.title) -> track
          }
        }
      }
    }

}

object LibraryMetadata {

  def apply(tracks: Seq[(Path, TrackMetadata)]): LibraryMetadata = {
    val (effectiveTracks, incompleteMetadataTracks) =
      tracks.partition { case (_, metadata) =>
        (metadata.artistName.isDefined || metadata.albumArtistName.isDefined) && metadata.title.isDefined
      }

    val artists =
      effectiveTracks
        .flatMap { case (path, metadata) => Track(path, metadata) }
        .groupBy(_.effectiveArtistName)
        .collect { case (Some(artistName), artistTracks) =>
          val (effectiveArtistTracks, uncategorizedTracks) = artistTracks.partition(_.albumName.isDefined)

          val artistAlbums =
            effectiveArtistTracks
              .groupBy(_.albumName)
              .collect { case (Some(albumName), albumTracks) =>
                Album(albumName, groupTracks(albumTracks))
              }
              .toSeq

          Artist(
            artistName,
            SeqMap.from {
              artistAlbums.view
                .sortBy(album => (album.year, album.name))
                .map(album => (album.name, album))
            },
            groupTracks(uncategorizedTracks)
          )
        }
        .toSeq

    LibraryMetadata(
      SeqMap.from {
        artists.view
          .sortBy(_.name)
          .map(artist => (artist.name, artist))
      },
      incompleteMetadataTracks
    )
  }

  private def groupTracks(tracks: Seq[Track]): SeqMap[TrackTitle, Track] =
    SeqMap.from {
      tracks.view
        .sortBy(track => (track.number, track.title))
        .map(track => (track.title, track))
    }

}
