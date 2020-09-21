package musicplayer.library.config

import musicplayer.library.model.MediaFormat

case class LibraryScannerConfig(supportedFormats: Set[MediaFormat],
                                followSymLinks: Boolean)
