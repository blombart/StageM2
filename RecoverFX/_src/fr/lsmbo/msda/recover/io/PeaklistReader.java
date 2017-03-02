package fr.lsmbo.msda.recover.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import fr.lsmbo.msda.recover.Session;
import fr.lsmbo.msda.recover.lists.Spectra;
import fr.lsmbo.msda.recover.model.Fragment;
import fr.lsmbo.msda.recover.model.Spectrum;

public class PeaklistReader {

	private static Boolean retentionTimesAreMissing = true;
	
	public static Boolean retentionTimesNotFound() {
		return retentionTimesAreMissing;
	}
	
	public static void load(File file) {
		String filePath = file.getAbsolutePath();
		Session.HIGHEST_FRAGMENT_MZ = 0F;
		Session.HIGHEST_FRAGMENT_INTENSITY = 0F;
		// start read file
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			// initialize spectrum list (or reset it if already exists)
			Spectra.initialize();
			
			// read chunks of file and load data in sqlite
			if(file.getName().endsWith(".mgf")) {
				readMgfFile(reader);
			} else if(file.getName().endsWith(".pkl")) {
				readPklFile(reader);
			}

			Session.CURRENT_FILE = file;
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readMgfFile(BufferedReader reader) throws IOException {
		String line;
		Integer lineNumber = 1;
		Spectrum spectrum = null;
		String textBeforeFirstSpectrum = "";
		Integer spectrumId = 0;
		Integer fragmentId = 0;
		while((line = reader.readLine()) != null) { // iterate through the lines of the file
			if(line.startsWith("BEGIN IONS")) {
				spectrumId++;
				spectrum = new Spectrum();
				spectrum.setLineStart(lineNumber);
				spectrum.setId(spectrumId);
			} else if(line.startsWith("TITLE")) {
				spectrum.setTitle(line.replaceFirst("TITLE.\\s+", ""));
				if(retentionTimesAreMissing && spectrum.getRetentionTime() > 0)
					retentionTimesAreMissing = false;
			} else if(line.startsWith("RTINSECONDS")) {
				// convert seconds in minutes
				spectrum.setRetentionTime(new Float(line.replaceFirst("RTINSECONDS=", "")) / 60);
				retentionTimesAreMissing = false;
			} else if(line.startsWith("PEPMASS")) {
				String[] items = line.replaceFirst("PEPMASS=", "").split("[\\t\\s]");
				spectrum.setMz(new Float(items[0]));
				spectrum.setIntensity(new Float(items[1]));
			} else if(line.startsWith("CHARGE") && spectrum != null) {
				spectrum.setCharge(new Integer(line.replaceFirst("CHARGE=", "").replaceAll("\\+", "")));
			} else if(line.startsWith("END IONS")) {
				spectrum.setLineStop(lineNumber);
				Spectra.add(spectrum);
			} else if(line.matches("^[\\d\\s\\t\\.\\+]+$")) {
				Fragment fragment = new Fragment();
				try { // just in case casting fails
					String[] items = line.split("[\\s\\t]");
					fragmentId++;
					fragment.setId(fragmentId);
					fragment.setMz(new Float(items[0]));
					fragment.setIntensity(new Float(items[1]));
					if(items.length == 3)
						fragment.setCharge(new Integer(items[2].replaceAll("\\+", "")));
					if(fragment.getMz() > Session.HIGHEST_FRAGMENT_MZ)
						fragment.setMz(Session.HIGHEST_FRAGMENT_MZ);
					if(fragment.getIntensity() > Session.HIGHEST_FRAGMENT_INTENSITY)
						fragment.setIntensity(Session.HIGHEST_FRAGMENT_INTENSITY);
				} catch(Exception e) {}
				spectrum.addFragment(fragment);
			} else if(spectrum == null) {
				textBeforeFirstSpectrum += line;
			}
			lineNumber++;
		}
		Session.FILE_HEADER = textBeforeFirstSpectrum;
	}
	
	private static void readPklFile(BufferedReader reader) throws IOException {
		String line;
		Integer lineNumber = 1;
		Spectrum spectrum = null;
		Integer spectrumId = 0;
		Integer fragmentId = 0;
		while((line = reader.readLine()) != null) { // iterate through the lines of the file
			if(line.isEmpty()) {
				// spectra are separated by a blank line
				if(spectrum != null) {
					// store the previous spectrum if any
					spectrum.setLineStop(lineNumber);
					Spectra.add(spectrum);
				}
				// reset spectrum
				spectrum = null;
			} else {
				String[] items = line.split("[\\t\\s]");
				if(items.length == 3) {
					spectrum = new Spectrum();
					spectrumId++;
					spectrum.setId(spectrumId);
					spectrum.setLineStart(lineNumber);
					try { // just in case casting fails
						spectrum.setMz(new Float(items[0]));
						spectrum.setIntensity(new Float(items[1]));
						spectrum.setCharge(new Integer(items[2]));
					} catch(Exception e) {}
				} else if(items.length == 2) {
					fragmentId++;
					Fragment fragment = new Fragment();
					fragment.setId(fragmentId);
					try { // just in case casting fails
						fragment.setMz(new Float(items[0]));
						fragment.setIntensity(new Float(items[1]));
						if(fragment.getMz() > Session.HIGHEST_FRAGMENT_MZ)
							fragment.setMz(Session.HIGHEST_FRAGMENT_MZ);
						if(fragment.getIntensity() > Session.HIGHEST_FRAGMENT_INTENSITY)
							fragment.setIntensity(Session.HIGHEST_FRAGMENT_INTENSITY);
					} catch(Exception e) {}
					spectrum.addFragment(fragment);
				} // other cases should never happen !
			}
			lineNumber++;
		}
		retentionTimesAreMissing = false; // actually there's no rt but if true it would trigger a dialogbox for parsing titles which would be useless
	}
	
}
