# CinematicCore

**CinematicCore** is a lightweight recording and playback plugin for Paper servers.  It
allows administrators to record player movements and play them back for other
players as smooth camera fly‐throughs.  The goal of this refactor is to make
the plugin more robust, easier to configure and extend, and to improve
playback quality.

## Features

* **JSON Lines storage** – Each cinematic is now stored as a separate
  `.jsonl` (or `.jsonl.gz`) file with one frame per line.  The first line of
  each file is a JSON metadata object containing the cinematic name, world,
  frame count and timestamp.  This makes it easy to append new frames without
  rewriting the entire file and opens the door for future streaming use.
* **Optional gzip compression** – When enabled in `config.yml` all JSONL
  files are transparently compressed.  Compression reduces disk usage and can
  improve I/O performance.
* **Configurable interpolation** – The number of interpolation steps between
  two recorded frames can be configured in `config.yml`.  Increasing the
  interpolation steps yields smoother camera movements at the cost of more
  teleport updates.  Setting the value to `1` disables interpolation.
* **Automatic cleanup on player quit** – Players are automatically removed
  from the active viewers set when they leave the server, ensuring that
  cinematics do not continue to run for offline players.

## Configuration

The plugin generates a `config.yml` on first run.  Important settings include:

* `storage.use-jsonl` – Persist cinematics as JSON Lines streams instead of a
  single monolithic JSON file.  Defaults to `true`.
* `storage.gzip` – Compress JSON Lines files using gzip.  Only applies when
  `use-jsonl` is enabled.  Defaults to `true`.
* `storage.folder` – Directory (inside the plugin data folder) where
  cinematic files are stored.  Defaults to `cinematics`.
* `playback.interpolation-steps` – Number of interpolation steps between two
  recorded frames.  Use higher values for smoother motion.  `1` disables
  interpolation.

## WYSIWYG editor (future idea)

As a complement to command‐line recording, an in‑game WYSIWYG editor could be
provided to fine‑tune keyframes.  The concept is to spawn a shulker bullet or
another visible marker at each keyframe.  Players could then interact with
these markers to adjust their position, orientation or attached commands.
Implementing such an editor would require additional commands, event
listeners and possibly a custom GUI.  This idea is currently left as a memo
for future contributors.
