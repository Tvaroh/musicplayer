package musicplayer.library.model

import java.nio.file.Path

import musicplayer.library.model.metadata.TrackMetadata

case class Track(metadata: TrackMetadata,
                 path: Path)
