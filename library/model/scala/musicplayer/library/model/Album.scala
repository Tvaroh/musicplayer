package musicplayer.library.model

import scala.collection.immutable.SeqMap

case class Album(name: AlbumName,
                 tracks: SeqMap[TrackTitle, Track]) {

  val year: Option[AlbumYear] = {
    val trackYears = tracks.view.flatMap(_._2.year).toSet

    if (trackYears.size == 1) trackYears.headOption.map(AlbumYear(_)) else None
  }

}
