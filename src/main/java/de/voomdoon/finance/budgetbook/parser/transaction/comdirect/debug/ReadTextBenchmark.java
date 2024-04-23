package de.voomdoon.finance.budgetbook.parser.transaction.comdirect.debug;

import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDPage;

import de.voomdoon.util.cli.Program;
import de.voomdoon.util.pdf.PdfReader;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class ReadTextBenchmark extends Program {

	/**
	 * DOCME add JavaDoc for method main
	 * 
	 * @param args
	 * @since 0.1.0
	 */
	public static void main(String[] args) {
		Program.run(args);
	}

	/**
	 * @since 0.1.0
	 */
	@Override
	protected void run() throws Exception {
		String filenName = pollArg("base-directory") + "/src/test/resources/Finanzreport_2017-06-02.pdf";

		PdfReader reader = new PdfReader(new File(filenName));

		run(reader, -1);

		Map<Integer, AtomicLong> times = new HashMap<>();

		for (int iLoad = 0; iLoad < 100; iLoad++) {
			for (int width = 0; width <= 500; width += 100) {
				logger.trace("width: " + width);
				long start = System.currentTimeMillis();
				run(reader, width);
				long end = System.currentTimeMillis();
				long duration = end - start;
				logger.trace("duration: " + duration);
				times.computeIfAbsent(width, key -> new AtomicLong()).addAndGet(duration);

				logger.info("durations:\n"
						+ times.entrySet().stream().sorted((e1, e2) -> Integer.compare(e1.getKey(), e2.getKey()))
								.map(e -> e.getKey() + "\t" + e.getValue()).collect(Collectors.joining("\n")));
			}
		}
	}

	private void run(PdfReader reader, int width) {
		for (int iPage = 0; iPage < reader.getDocument().getPages().getCount(); iPage++) {
			PDPage page = reader.getDocument().getPage(iPage);

			for (int y = (int) page.getMediaBox().getHeight(); y > 0; y--) {
				reader.readText(iPage,
						new Rectangle(0, y, width == -1 ? (int) page.getMediaBox().getWidth() : width, y));
			}

			// break;// DEBUG
		}
	}
}
