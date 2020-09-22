package musicplayer.library.config

import musicplayer.library.model.MediaFormat

case class LibraryConfig(supportedFormats: Set[MediaFormat],
                         followSymLinks: Boolean)
