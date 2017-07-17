# Build DMS binaries

These scripts are only meant for enthusiasts who want to bundle their DMS with
custom built versions of libraries and tools, replacing the standard versions
shipped with the regular DMS distribution.

There are two scripts available: the first for downloading the sources and the
second for building the sources into binaries

After running the first two scripts the following directory structure is created:

    DigitalMediaServer/
      |
      +-- contrib/
      |     |
      |     +-- binaries-deps-versions
      |     +-- build-dms-binaries.sh
      |     +-- download-dms-binaries-source.sh 
      |
      +-- target/
            |
            +-- bin-tools/
                  |
                  +-- build/
                  +-- src/
                  +-- target/
                        |
                        +-- bin/
                        +-- lib/

Search `../target/bin-tools/target/bin/` for compiled binaries and
`../target/bin-tools/target/lib/` for libraries.


## Downloading (and updating) sources
This script downloads the sources for the binaries and libraries:

    download-dms-binaries-source.sh

Run the script and the source archives and directories will be stored in
`../target/bin-tools/src/`.


## Building binaries
This script builds binaries from the sources that were downloaded with the
other script:

    build-dms-binaries.sh


## Cleaning up
To clean binaries built by the scripts, remove the following directories:

    rm -rf ../target/bin-tools/build/
    rm -rf ../target/bin-tools/target/
