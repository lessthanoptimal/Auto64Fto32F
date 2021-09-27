# Setup

1) Make an application which configures then runs `RuntimeRegressionMasterApp`
2) Modify `build.gradle` to include JMH source sets
3) Create a Gradle task which runs the application

## Writing Application

Example from BoofCV

```java
public class BoofCVRuntimeRegressionApp {
	public static void main( String[] args ) {
		// Set up the environment
		ProjectUtils.checkRoot = ( f ) ->
				new File(f, "README.md").exists() && new File(f, "settings.gradle").exists();
		ProjectUtils.sourceInfo = () -> {
			var info = new LibrarySourceInfo();
			info.version = BoofVersion.VERSION;
			info.gitDate = BoofVersion.GIT_DATE;
			info.gitSha = BoofVersion.GIT_SHA;
			info.projectName = "BoofCV";
			return info;
		};

		// Specify which packages it should skip over
		String[] excluded = new String[]{"autocode", "checks", "boofcv-types", "boofcv-core"};
		ProjectUtils.skipTest = ( f ) -> {
			for (String name : excluded) {
				if (f.getName().equals(name))
					return true;
			}
			return false;
		};

		RuntimeRegressionMasterApp.main(args);
	}
}
```

## Modify Gradle to Reference JMH Source Set

```gradle
dependencies {
    runtime project(':main:boofcv-feature').sourceSets.benchmark.output
}
```

## Creating Gradle Task

The easiest way to include all the benchmarks is to run your application from a
Gradle task. This does make command line arguments a bit of a kludge

```gradle
// Run the regression using a gradle command
// Currently this is the only way to get paths set up for benchmarks. See comment below.
//
// Example: ./gradlew runtimeRegression run --console=plain -Dexec.args="--SummaryOnly"
task runtimeRegression(type: JavaExec) {
	dependsOn build
	group = "Execution"
	description = "Run the mainClass from the output jar in classpath with ExecTask"
	classpath = sourceSets.main.runtimeClasspath
	main = "boofcv.regression.BoofCVRuntimeRegressionApp"
	args System.getProperty("exec.args", "").split()
}
```