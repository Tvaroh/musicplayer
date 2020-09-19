package musicplayer.library

import taggedtypes.TaggedType

package object model {

  object ArtistName extends TaggedType[String]
  type ArtistName = ArtistName.Type

  object AlbumName extends TaggedType[String]
  type AlbumName = AlbumName.Type

  object AlbumYear extends TaggedType[Int]
  type AlbumYear = AlbumYear.Type

  object TrackName extends TaggedType[String]
  type TrackName = TrackName.Type

  object TrackNumber extends TaggedType[Int]
  type TrackNumber = TrackNumber.Type

  object TrackYear extends TaggedType[Int]
  type TrackYear = TrackYear.Type

}
