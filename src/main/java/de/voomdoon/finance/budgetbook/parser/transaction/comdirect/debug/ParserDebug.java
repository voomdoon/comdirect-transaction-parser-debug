package de.voomdoon.finance.budgetbook.parser.transaction.comdirect.debug;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import de.voomdoon.finance.budgetbook.model.BankStatementTransaction;
import de.voomdoon.finance.budgetbook.parser.transaction.comdirect.ComdirectTransactionParser;
import de.voomdoon.util.cli.Program;
import de.voomdoon.util.csv.writer.CsvWriter;
import de.voomdoon.util.csv.writer.CsvWriterBuilder;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class ParserDebug extends Program {

	/**
	 * @since 0.1.0
	 */
	private static final Comparator<File> COMPARATOR = (f1, f2) -> {
		LocalDate d1 = parseLocalDate(f1.getName());
		LocalDate d2 = parseLocalDate(f2.getName());

		return d1.compareTo(d2);
	};

	/**
	 * @since 0.1.0
	 */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	/**
	 * @param args
	 * @since 0.1.0
	 */
	public static void main(String[] args) {
		Program.run(args);
	}

	/**
	 * DOCME add JavaDoc for method formatAmount
	 * 
	 * @param amount
	 * @return
	 * @since 0.1.0
	 */
	private static String formatAmount(Long amount) {
		String string = Long.toString(amount);

		return string.substring(0, string.length() - 2) + "." + string.substring(string.length() - 2);
	}

	/**
	 * DOCME add JavaDoc for method parseLocalDate
	 * 
	 * @param name
	 * @return
	 * @since 0.1.0
	 */
	private static LocalDate parseLocalDate(String name) {
		int start = name.indexOf("per_") + 4;
		int end = start + 10;

		return LocalDate.parse(name.substring(start, end), FORMATTER);
	}

	/**
	 * @since 0.1.0
	 */
	private String inputDirectoryName;

	/**
	 * @since 0.1.0
	 */
	private String outputCsvDirectoryName;

	/**
	 * @since 0.1.0
	 */
	private String outputPdfDirectoryName;

	/**
	 * @since 0.1.0
	 */
	@Override
	protected void run() throws Exception {
		String baseDirectory = pollArg("base-directory");
		inputDirectoryName = new File(baseDirectory + "/input/André").toString();
		outputPdfDirectoryName = baseDirectory + "/output/pdf";
		outputCsvDirectoryName = baseDirectory + "/output/csv";

		IOFileFilter fileFilter = new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().startsWith("Finanzreport") && file.getName().endsWith(".pdf");
			}

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("Finanzreport") && name.endsWith(".pdf");
			}
		};

		Collection<File> files = FileUtils.listFiles(new File(inputDirectoryName), fileFilter,
				DirectoryFileFilter.DIRECTORY);
		files = files.stream().sorted(COMPARATOR).toList();

		for (File file : files) {
			process(file);
		}
	}

	/**
	 * DOCME add JavaDoc for method getOutputCvsFileName
	 * 
	 * @param input
	 * @return
	 * @since 0.1.0
	 */
	private String getOutputCvsFileName(String input) {
		return outputCsvDirectoryName + "/"
				+ input.substring(inputDirectoryName.length() + 1, input.length()).replace(".pdf", ".csv");
	}

	/**
	 * DOCME add JavaDoc for method getDebugFileName
	 * 
	 * @param input
	 * @return
	 * @since 0.1.0
	 */
	private String getOutputPdfFileName(String input) {
		return outputPdfDirectoryName + "/"
				+ input.substring(inputDirectoryName.length() + 1, input.length()).replace(".pdf", "_parsed.pdf");
	}

	/**
	 * DOCME add JavaDoc for method process
	 * 
	 * @param input
	 * @throws Exception
	 * @since 0.1.0
	 */
	private void process(File input) throws Exception {
		String outputPdfFileName = getOutputPdfFileName(input.toString());
		String outputCvsFileName = getOutputCvsFileName(input.toString());
		logger.info("process " + input + " -> " + outputCvsFileName);

		ComdirectTransactionParser parser = new ComdirectTransactionParser(input.toString());

		List<BankStatementTransaction> transactions = null;
		Exception exception = null;

		try {
			transactions = parser.parseTransactions();
		} catch (Exception e) {
			exception = e;
		}

		parser.saveDebug(outputPdfFileName);

		if (exception != null) {
			throw new RuntimeException("Failed to process " + input + "!", exception);
		}

		saveCsv(outputCvsFileName, transactions);
	}

	/**
	 * DOCME add JavaDoc for method saveCsv
	 * 
	 * @param output
	 * @param transactions
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void saveCsv(String output, List<BankStatementTransaction> transactions) throws IOException {
		CsvWriter writer = new CsvWriterBuilder(output).build();
		writer.writeRow(List.of("booking date", "valuta", "other account", "what", "amount"));

		for (BankStatementTransaction transaction : transactions) {
			writer.writeRow(toRow(transaction));
		}

		writer.close();
	}

	/**
	 * DOCME add JavaDoc for method toRow
	 * 
	 * @param transaction
	 * @return
	 * @since 0.1.0
	 */
	private List<String> toRow(BankStatementTransaction transaction) {
		return List.of(transaction.getBookingDate().toString(), transaction.getValuta().toString(),
				transaction.getOtherAccount(), transaction.getWhat(), formatAmount(transaction.getAmount()));
	}
}
