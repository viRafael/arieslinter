# Cutting a release

The installer downloads a **pinned** JAR, so a release is not finished until the pins are updated.

1. Decide the version (SemVer) and set `<version>` in `pom.xml`. The tag, the pom version and the JAR name must agree (today the tag is `1.5` but the JAR is `arieslinter-1.0.jar`; fix this on the next release).
2. `mvn -B clean package`, then run the corpus check and the unit tests (see `CONTRIBUTING.md`).
3. Create the tag and a GitHub release and attach the JAR. **Do not re-upload or replace assets of a published release**: publish a new version instead.
4. Compute `sha256sum arieslinter-<version>.jar`, attach a `SHA256SUMS` file to the release too.
5. In `install.py` update `ARIES_TAG`, `ARIES_JAR_NAME`, `ARIES_SHA256` (and `CHECKSTYLE_*` if the Checkstyle version changes), then update `README.md`, `CHANGELOG.md` and `docs/`.
6. Run `python3 install.py --project <scratch dir> --ide none` and confirm the verification step passes.
7. Publish a new Zenodo version of the artifact (the current record is from August 2025).
