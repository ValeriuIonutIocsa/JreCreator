package com.personal.jre_creator;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.utils.io.PathUtils;
import com.utils.io.processes.InputStreamReaderThread;
import com.utils.io.processes.ReadBytesHandlerLinesCollect;
import com.utils.log.Logger;

final class AppStartJreCreator {

	private AppStartJreCreator() {
	}

	public static void main(
			final String[] args) {

		final Instant start = Instant.now();

		if (args.length >= 1 && "-help".equals(args[0])) {

			final String helpMessage = createHelpMessage();
			Logger.printLine(helpMessage);
			System.exit(0);
		}

		if (args.length < 2) {

			final String helpMessage = createHelpMessage();
			Logger.printError("insufficient arguments" +
					System.lineSeparator() + helpMessage);
			System.exit(-1);
		}

		final String jdkFolderPathString = args[0];
		final String outputJreFolderPathString = args[1];

		mainL2(jdkFolderPathString, outputJreFolderPathString);

		Logger.printFinishMessage(start);
	}

	private static String createHelpMessage() {

		return "usage: jre_creator <jdk_folder_path> <output_jre_folder_path>";
	}

	private static void mainL2(
			final String jdkFolderPathString,
			final String outputJreFolderPathString) {

		Logger.printProgress("starting JreCreator");
		Logger.printLine("jdkFolderPathString = " + jdkFolderPathString);
		Logger.printLine("outputJreFolderPathString = " + outputJreFolderPathString);

		final List<String> jdkModuleList = parseJdkModules(jdkFolderPathString);
		if (jdkModuleList.isEmpty()) {
			Logger.printError("failed to parse JDK modules");

		} else {
			createJre(jdkFolderPathString, jdkModuleList, outputJreFolderPathString);
		}
	}

	private static List<String> parseJdkModules(
			final String jdkFolderPathString) {

		final List<String> jdkModuleList = new ArrayList<>();
		try {
			final String javaExePathString = PathUtils.computePath(jdkFolderPathString, "bin", "java.exe");
			final String[] commandPartArray = { javaExePathString, "--list-modules" };

			Logger.printProgress("executing command:");
			Logger.printLine(StringUtils.join(commandPartArray, ' '));

			final Process process = new ProcessBuilder()
					.command(commandPartArray)
					.redirectErrorStream(true)
					.start();

			final ReadBytesHandlerLinesCollect readBytesHandlerLinesCollect = new ReadBytesHandlerLinesCollect();
			final InputStreamReaderThread inputStreamReaderThread =
					new InputStreamReaderThread("list JDK modules", process.getInputStream(),
							StandardCharsets.UTF_8, readBytesHandlerLinesCollect);
			inputStreamReaderThread.start();

			process.waitFor();
			inputStreamReaderThread.join();

			final List<String> lineList = readBytesHandlerLinesCollect.getLineList();
			for (final String line : lineList) {

				final int indexOf = line.indexOf('@');
				if (indexOf > 0) {

					final String jdkModule = line.substring(0, indexOf);
					jdkModuleList.add(jdkModule);
				}
			}

		} catch (final Exception exc) {
			Logger.printException(exc);
		}
		return jdkModuleList;
	}

	private static void createJre(
			final String jdkFolderPathString,
			final List<String> jdkModuleList,
			final String outputJreFolderPathString) {

		try {
			final List<String> commandPartList = new ArrayList<>();

			final String jlinkExePathString = PathUtils.computePath(jdkFolderPathString, "bin", "jlink.exe");
			commandPartList.add(jlinkExePathString);

			commandPartList.add("--no-header-files");
			commandPartList.add("--no-man-pages");
			commandPartList.add("--compress=2");
			commandPartList.add("--add-modules");

			commandPartList.add(StringUtils.join(jdkModuleList, ','));

			commandPartList.add("--output");
			commandPartList.add(outputJreFolderPathString);

			Logger.printProgress("executing command:");
			Logger.printLine(StringUtils.join(commandPartList, ' '));

			final Process process = new ProcessBuilder()
					.command(commandPartList)
					.redirectErrorStream(true)
					.start();
			process.waitFor();

		} catch (final Exception exc) {
			Logger.printException(exc);
		}
	}
}
