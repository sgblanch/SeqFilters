package edu.msu.cme.rdp.initprocess;

import edu.msu.cme.rdp.initprocess.InitialProcessOptions.GENENAME;
import java.io.File;
import java.io.IOException;

import edu.msu.cme.rdp.readseq.SequenceParsingException;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.utils.BarcodeSorter;
import edu.msu.cme.rdp.readseq.utils.BarcodeUtils.BarcodeInvalidException;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import edu.msu.cme.rdp.seqfilter.SeqFilterChain;
import edu.msu.cme.rdp.seqfilter.SeqFilteringResult;
import edu.msu.cme.rdp.seqfilter.filters.ExpQualityFilter;
import edu.msu.cme.rdp.seqfilter.filters.NSeqFilter;
import edu.msu.cme.rdp.seqfilter.filters.PrimerFilter;
import edu.msu.cme.rdp.seqfilter.filters.PrimerFilter.PrimerFilterListener;
import edu.msu.cme.rdp.seqfilter.filters.SeqLengthFilter;
import edu.msu.cme.rdp.seqfilter.filters.SeqLengthFilter.LengthFilterType;
import edu.msu.cme.rdp.seqfilter.filters.ValidAlphabetFilter;
import edu.msu.cme.rdp.seqfilter.filters.ValidAlphabetFilter.ValidAlphabetFilterMode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InitialProcessor {

    private File userTempDir;
    private File tagSortDir;
    private File resultDir;
    private File tagFile;
    private InitialProcessOptions initialProcessOptions;
    private static final String TAGSORT_DIR = "tagsort_dir";
    private Map<String, InitialProcessOptions> customOptsMap = new HashMap();

    public InitialProcessor(String resultDirName, File userTempDir, File tagFile, InitialProcessOptions seqTrimCmdOption) throws IOException {
        this.userTempDir = userTempDir;
        this.tagFile = tagFile;
        this.initialProcessOptions = seqTrimCmdOption;
        this.tagSortDir = new File(this.userTempDir, TAGSORT_DIR);
        this.resultDir = new File(this.userTempDir, resultDirName);

        if (!this.tagSortDir.exists()) {
            if (!tagSortDir.mkdir()) {
                throw new IOException("Failed to make tagsort dir " + tagSortDir.getAbsolutePath());
            }
        }

        if (!this.resultDir.exists()) {
            if (!resultDir.mkdir()) {
                throw new IOException("Failed to make result dir " + resultDir.getAbsolutePath());
            }
        }

        this.customOptsMap = loadCustomOpts(tagFile, seqTrimCmdOption);
    }

    private static InitialProcessOptions cloneOptions(InitialProcessOptions o) {
        InitialProcessOptions ret = new InitialProcessOptions();

        ret.bestScoreOutfile = o.bestScoreOutfile;
        ret.fPrimer = o.fPrimer;
        ret.forwardMaxEditDist = o.forwardMaxEditDist;
        ret.genename = o.genename;
        ret.keepPrimers = o.keepPrimers;
        ret.minExpQualScore = o.minExpQualScore;
        ret.minSeqLength = o.minSeqLength;
        ret.noofns = o.noofns;
        ret.processNoTag = o.processNoTag;
        ret.qualInfile = o.qualInfile;
        ret.rPrimer = o.rPrimer;
        ret.reverseMaxEditDist = o.reverseMaxEditDist;
        ret.seqInfile = o.seqInfile;
        ret.trimQualOutfile = o.trimQualOutfile;
        ret.trimSeqOutfile = o.trimSeqOutfile;

        return ret;
    }

    private static Map<String, InitialProcessOptions> loadCustomOpts(File tagfile, InitialProcessOptions defaultOpts) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(tagfile));
        String line;
        Map<String, InitialProcessOptions> ret = new HashMap();

        while ((line = reader.readLine()) != null) {
            String[] lexemes = line.split("\\s+");

            if (lexemes.length == 3) {
                String sampleName = lexemes[1];
                String[] optsList = lexemes[2].split(",");

                InitialProcessOptions opts = cloneOptions(defaultOpts);
                for (String opt : optsList) {
                    if (!opt.contains("=")) {
                        System.err.println("Couldn't process opt " + opt);
                        continue;
                    }

                    String[] optLexemes = opt.split("=");
                    String name = optLexemes[0];
                    String val = "";
                    if (optLexemes.length > 1) {
                        val = optLexemes[1];
                    }

                    if (name.equals("fprimer")) {
                        opts.fPrimer = val.split("\\|");
                    } else if (name.equals("rprimer")) {
                        opts.rPrimer = val.split("\\|");
                    } else if (name.equals("fedit")) {
                        opts.forwardMaxEditDist = Integer.valueOf(val);
                    } else if (name.equals("redit")) {
                        opts.reverseMaxEditDist = Integer.valueOf(val);
                    } else if (name.equals("gene")) {
                        opts.genename = GENENAME.valueOf(val);
                    } else if (name.equals("keep_primer")) {
                        opts.keepPrimers = Boolean.valueOf(val);
                    } else if (name.equals("min_qual")) {
                        opts.minExpQualScore = Integer.valueOf(val);
                    } else if (name.equals("min_length")) {
                        opts.minSeqLength = Integer.valueOf(val);
                    } else if (name.equals("max_ns")) {
                        opts.noofns = Integer.valueOf(val);
                    } else {
                        System.err.println("Unprocessed option " + opt);
                    }
                }

                ret.put(sampleName, opts);
            }
        }

        return ret;
    }

    private static void printParams(InitialProcessOptions params, File f) throws IOException {
        PrintStream out = new PrintStream(f);

        out.println("Input file: " + new File(params.seqInfile).getName());

        if (params.qualInfile == null || params.qualInfile.equals("") || !new File(params.qualInfile).exists()) {
            out.println("Quality file: (None)");
        } else {
            out.println("Quality file: " + new File(params.qualInfile).getName());
        }

        out.println("Gene name: " + params.genename.toString());
        out.println("Maximum number of Ns: " + params.noofns);
        out.println("Minimum sequence length: " + params.minSeqLength);
        out.println("Minimum exponential Q-score: " + params.minExpQualScore);
        out.println("Primers removed?: " + !params.keepPrimers);
        out.println("Process no tag?: " + params.processNoTag);
        out.println("Forward primer(s): " + Arrays.asList(params.fPrimer).toString().replace("[", "").replace("]", ""));
        out.println("Max forward primer distance: " + params.forwardMaxEditDist);

        if (params.rPrimer == null || params.rPrimer.length == 0 || (params.rPrimer.length == 1 && (params.rPrimer[0] == null || params.rPrimer[0].equals("")))) {
            out.println("Reverse primer(s): (None)");
        } else {
            out.println("Reverse primer(s): " + Arrays.asList(params.rPrimer).toString().replace("[", "").replace("]", ""));
            out.println("Max reverse primer distance: " + params.reverseMaxEditDist);
        }

        out.close();
    }

    public void runSeqTrim() throws IOException, SequenceParsingException {
        Set<String> takenTagNames = new HashSet();
        for (File seqFile : tagSortDir.listFiles()) {
            SequenceFormat format = SeqUtils.guessFileFormat(seqFile);
            if (format != SequenceFormat.UNKNOWN) {
                String tagName = seqFile.getName().substring(0, seqFile.getName().lastIndexOf("."));

                if (takenTagNames.contains(tagName)) {
                    throw new IOException("Two files from tag " + tagName);
                }

                if (tagName.equals(BarcodeSorter.NoTag) && !this.initialProcessOptions.processNoTag) {
                    continue;
                }

                File sampleDir = new File(this.resultDir, tagName);

                if (!sampleDir.exists()) {
                    sampleDir.mkdir();
                }

                File trimSeqOutfile = new File(sampleDir, tagName + "_trimmed.fasta");
                File bestScoreOutfile = new File(sampleDir, tagName + "_bestscore.txt");
                File trimQualOutfile = new File(sampleDir, tagName + "_trimmed.qual");
                File lengthHistogramFile = new File(sampleDir, tagName + "_length_histo.png");
                File qualityChartFile = new File(sampleDir, tagName + "_quality.png");
                File summaryFile = new File(sampleDir, tagName + "_summary.txt");
                File lengthStatsFile = new File(sampleDir, tagName + "_length_stats.txt");
                File qualStatsFile = new File(sampleDir, tagName + "_qual_stats.txt");
                File fileredSeqsFile = new File(sampleDir, tagName + "_dropped_seqs.txt");

                final PrintStream bestScoresOut = new PrintStream(bestScoreOutfile);
                PrimerFilterListener l = new PrimerFilterListener() {

                    public void sequencePassed(String seqName, int seqStart, int seqStop, int forwardPrimer, int reversePrimer, int forwardScore, int reverseScore, int partialScore, int ns, int length, boolean reversed) {
                        bestScoresOut.println(seqName
                                + "|fPrimer|" + forwardPrimer
                                + "|fPrimer.bestScore|" + forwardScore
                                + "|rPrimer|" + reversePrimer
                                + "|rPrimer.bestScore|" + reverseScore
                                + "|dpscore|" + partialScore
                                + "|noofns|" + ns
                                + "|trimmed_len|" + length
                                + "|reverse|" + reversed);
                    }
                };

                InitialProcessOptions options = initialProcessOptions;

                if (customOptsMap.containsKey(tagName)) {
                    options = customOptsMap.get(tagName);
                    printParams(options, new File(sampleDir, "custom_options.txt"));
                }

                SeqFilterChain chain = new SeqFilterChain(
                        //new CharReplaceFilter(CharReplaceFilter.rnaReplaceMap),
                        new ValidAlphabetFilter(ValidAlphabetFilterMode.DROP_SEQUENCE, SeqUtils.RNAAlphabet),
                        new PrimerFilter(options.fPrimer, options.rPrimer, options.forwardMaxEditDist, options.reverseMaxEditDist, options.genename == GENENAME.RRNA16S, options.keepPrimers, l),
                        new NSeqFilter(options.noofns),
                        new SeqLengthFilter(options.minSeqLength, LengthFilterType.GreaterThan),
                        new ExpQualityFilter(options.minExpQualScore) //new CaseTransformFilter()
                        );


                InitProcessOutput out = new InitProcessOutput(trimSeqOutfile, trimQualOutfile);

                SeqFilteringResult results = chain.filterSeqs(seqFile, out);
                out.close();
                bestScoresOut.close();

                out.writeStats(fileredSeqsFile, summaryFile, lengthStatsFile, lengthHistogramFile, qualStatsFile, qualityChartFile, tagName, results);
                if (trimQualOutfile.length() == 0) {
                    trimQualOutfile.delete();
                }
            }
        }

        printParams(this.initialProcessOptions, new File(this.resultDir, "input_params.txt"));
    }

    public void go() throws IOException, SequenceParsingException, BarcodeInvalidException {
        if (this.initialProcessOptions.qualInfile != null) {
            BarcodeSorter.sortWithQual(new File(this.initialProcessOptions.seqInfile), new File(this.initialProcessOptions.qualInfile), this.tagFile, this.tagSortDir);
        } else {
            BarcodeSorter.sortWithQual(new File(this.initialProcessOptions.seqInfile), null, this.tagFile, this.tagSortDir);
        }

        // create
        runSeqTrim();
    }

    public File getResultDir() {
        return this.resultDir;
    }
}
