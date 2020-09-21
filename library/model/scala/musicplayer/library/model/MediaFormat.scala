package musicplayer.library.model

sealed abstract class MediaFormat(val lowercaseExtension: String)

object MediaFormat {

  case object Flac extends MediaFormat("flac")
  case object Mp3 extends MediaFormat("mp3")

  val All: Set[MediaFormat] =
    Set(Flac, Mp3)

  def fromFilename(filename: String): Option[MediaFormat] =
    All.find(format => filename.toLowerCase.endsWith(s".${format.lowercaseExtension}"))

}
