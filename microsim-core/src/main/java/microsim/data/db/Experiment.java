package microsim.data.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="jasmine_experiment")
public class Experiment {

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long id;
	
	@Column(name="time_stamp")
	@Temporal(value=TemporalType.TIMESTAMP)
	public Date timestamp;
	
	@Column(name="run_id")
	public String runId;
	
	@Column(name="multi_run_id")
	public String multiRunId;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "experiment", orphanRemoval = true, cascade={CascadeType.ALL})
	@Fetch(FetchMode.SELECT)
	public List<ExperimentParameter> parameters;
	
	@Transient
	public String inputFolder = "./input";
	
	@Transient
	public String outputRootFolder = "./output";
	
	public String getOutputFolder() {
		if (runId == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			runId = sdf.format(new Date());
		}
		// multiRunId represents the seed of this run
		return outputRootFolder + File.separatorChar + runId + "_" + multiRunId;
	}
	
}
