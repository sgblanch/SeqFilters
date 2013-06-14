package edu.msu.cme.rdp.initprocess;

/**
 * 
 * @author siddiq15
 *
 */
public class InitialProcessOptions {

    public static enum GENENAME {

        RRNA16S, OTHER
    };

    public String[] fPrimer;
    public String[] rPrimer = {""};  // assign default
    public int forwardMaxEditDist;
    public int reverseMaxEditDist;
    public int noofns;
    public int minSeqLength;
    public String seqInfile;
    public String trimSeqOutfile;
    public String qualInfile;
    public String trimQualOutfile;
    public String bestScoreOutfile;
    public GENENAME genename;
    public boolean keepPrimers = false;
    public int minExpQualScore;
    public boolean processNoTag = true;

    public String getBestScoreOutfile() {
        return bestScoreOutfile;
    }

    public void setBestScoreOutfile(String bestScoreOutfile) {
        this.bestScoreOutfile = bestScoreOutfile;
    }

    public String[] getfPrimer() {
        return fPrimer;
    }

    public void setfPrimer(String[] fPrimer) {
        this.fPrimer = fPrimer;
    }

    public int getForwardMaxEditDist() {
        return forwardMaxEditDist;
    }

    public void setForwardMaxEditDist(int forwardMaxEditDist) {
        this.forwardMaxEditDist = forwardMaxEditDist;
    }

    public GENENAME getGenename() {
        return genename;
    }

    public void setGenename(GENENAME genename) {
        this.genename = genename;
    }

    public boolean isKeepPrimers() {
        return keepPrimers;
    }

    public void setKeepPrimers(boolean keepPrimers) {
        this.keepPrimers = keepPrimers;
    }

    public int getMinExpQualScore() {
        return minExpQualScore;
    }

    public void setMinExpQualScore(int minExpQualScore) {
        this.minExpQualScore = minExpQualScore;
    }

    public int getMinSeqLength() {
        return minSeqLength;
    }

    public void setMinSeqLength(int minSeqLength) {
        this.minSeqLength = minSeqLength;
    }

    public int getNoofns() {
        return noofns;
    }

    public void setNoofns(int noofns) {
        this.noofns = noofns;
    }

    public boolean isProcessNoTag() {
        return processNoTag;
    }

    public void setProcessNoTag(boolean processNoTag) {
        this.processNoTag = processNoTag;
    }

    public String getQualInfile() {
        return qualInfile;
    }

    public void setQualInfile(String qualInfile) {
        this.qualInfile = qualInfile;
    }

    public String[] getrPrimer() {
        return rPrimer;
    }

    public void setrPrimer(String[] rPrimer) {
        this.rPrimer = rPrimer;
    }

    public int getReverseMaxEditDist() {
        return reverseMaxEditDist;
    }

    public void setReverseMaxEditDist(int reverseMaxEditDist) {
        this.reverseMaxEditDist = reverseMaxEditDist;
    }

    public String getSeqInfile() {
        return seqInfile;
    }

    public void setSeqInfile(String seqInfile) {
        this.seqInfile = seqInfile;
    }

    public String getTrimQualOutfile() {
        return trimQualOutfile;
    }

    public void setTrimQualOutfile(String trimQualOutfile) {
        this.trimQualOutfile = trimQualOutfile;
    }

    public String getTrimSeqOutfile() {
        return trimSeqOutfile;
    }

    public void setTrimSeqOutfile(String trimSeqOutfile) {
        this.trimSeqOutfile = trimSeqOutfile;
    }
}
