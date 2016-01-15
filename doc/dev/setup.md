Development Setup
=================

This file describes how you might set up your development environment to view or edit the Boa compiler's source code.


Eclipse From a Git Repository
-----------------------------

1. Import the a local Git repo:
    - Select "File > Import".
    - Select "Git > Projects from Git".
    - Point Eclipse towards a repo with either "Existing local repository" or "Clone URI".
    - Select "Import using the New Project wizard", and click "Finish".

2. Make sure that the project has been cleaned with `ant clean`, and then manually add an empty `build/classes` directory to the repo. (For example, `cd` to the root of the repo, and run `ant clean; mkdir -p build/classes`).

3. Complete the import by creating a new Eclipse project:
    - Select "Java Project from Existing Ant Buildfile".
    - Browse to and select `build.xml` in the newly cloned directory.
    - You should now see a few "javac tasks".
    - **Important:** Check "Link to the buildfile in the file system".
    - From the root of the repo, run both `ant compile`.
    - Select "Finish".

4. Use a CLI to compile the project.
    - `cd` to the repo's root.
    - `ant compile`.

5. Tidy up the project's source directories:
    - From the `boa` project's "Properties", select the "Source" tab.
    - Use "Link Source..." until each folder under `src` (e.g. `java` and `compiled-proto`) is a source folder on the build path.
    - Use "Link Source..." again to add `build/java` as a source folder. Note that because the name `java` has already been used, you will need to name this source something else (e.g. `java-gen-src`).
    - Your package explorer should now look something like this:

    ![The Boa compiler as an Eclipse project from the package explorer view](img/eclipse_package_explorer_final.png)

6. Configure `ant` as an external tool so that you can trigger parts of the build which Eclipse can't perform (e.g. `compile-protobuf` and `compile-parser`).
    - Enter "External Tools Configuration".
    - Select or create an "Ant Build" launch configuration on the left (e.g. `boa build.xml`).
    - Set its "Base Directory" to be the root of the Boa compiler's repository.
    - Click "Apply".
