package org.jlab.service.eb;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.rec.eb.EBScalers;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author baltzell
 */
public class EBTBRerunEngine extends EBEngine {

    // static to store across events:
    static EBScalers ebScalers = new EBScalers();

    public EBTBRerunEngine(){
        super("EBTBRerun");
        setUsePOCA(false);
        bankType=DetectorResponse.BANK_TYPE_DST;
        dropBanks=true;
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
    }

    @Override
    public void initBankNames() {
        this.setEventBank("REC::Event");
        this.setParticleBank("REC::Particle");
        this.setEventBankFT("RECFT::Event");
        this.setParticleBankFT("RECFT::Particle");
        this.setCalorimeterBank("REC::Calorimeter");
        this.setCherenkovBank("REC::Cherenkov");
        this.setScintillatorBank("REC::Scintillator");
        this.setTrackBank("REC::Track");
        this.setCrossBank("REC::TrackCross");
        this.setCovMatrixBank("REC::CovMat");
        this.setTrajectoryBank("REC::Traj");        
        this.setFTBank("REC::ForwardTagger");

       
        this.dcTrackBankIn="REC::Track";
        this.dcTrajBankIn="REC::Traj";
        this.dcCovMatBankIn="REC::CovMat";
        this.ftofBankIn="REC::Scintillator";
        this.ecClusterBankIn="REC::Calorimeter";
        this.ecMomentsBankIn="REC::Calorimeter";
        this.ctofBankIn="REC::Scintillator";
        this.cndBankIn="REC::Scintillator";
        this.htccBankIn="REC::Cherenkov";
        this.ltccBankIn="REC::Cherenkov";
    }
    
}
