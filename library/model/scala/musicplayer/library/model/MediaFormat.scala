package musicplayer.library.model

sealed abstract class MediaFormat(val lowercaseExtension: String)

object MediaFormat {

  case object Flac extends MediaFormat("flac")

  val All: Set[MediaFormat] =
    Set(Flac)

  def fromFilename(filename: String): Option[MediaFormat] =
    All.find(format => filename.toLowerCase.endsWith(s".${format.lowercaseExtension}"))

}
