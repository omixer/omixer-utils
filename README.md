# omixer-utils

Small, generic Java helpers for reading and writing text files, used by
[Omixer-RPM](https://github.com/raeslab/omixer-rpm). They read and pass on lines; they never interpret them.

- `FileUtils`: delimited key-value files and rows, objects written one per line, and matrices with a header line —
  collected (`readMatrix`) or streamed one row at a time to a `MatrixRowHandler` (`readMatrixRows`), with each row's
  file name and line number (`FileLine`) for error messages.
- `CompressUtils`: gzip, gunzip, zip and unzip.
- `MathUtils`: fold-change ratios and cumulative sums.

## Build

Java 17 or newer; Gradle 9.

```
gradle build                 # compile and test
gradle publishToMavenLocal   # install io.github.omixer:omixer-utils into ~/.m2 (unsigned)
```

Released versions are on Maven Central as `io.github.omixer:omixer-utils` (the Java packages are `io.omixer.utils`).
To release, set `version` in `build.gradle`, commit, then push a matching tag: `git tag v1.2.0 && git push origin v1.2.0`.
The `Publish to Maven Central` workflow (`.github/workflows/publish.yml`) checks that the tag matches the version,
builds, tests, signs and publishes. It needs the repository secrets `MAVEN_CENTRAL_USERNAME` and
`MAVEN_CENTRAL_PASSWORD` (a Central Portal user token), `SIGNING_KEY` (`gpg --export-secret-keys --armor <key id>`) and
`SIGNING_KEY_PASSWORD`. A version on Maven Central can never be changed or removed.

Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## License

MIT, see [LICENSE](LICENSE).
