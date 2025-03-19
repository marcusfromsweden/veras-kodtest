package com.infrasight.kodtest;

/**
 * Spins up a KodtestServer
 */
public class RunServer {

	public static void main(String[] args) {
		KodtestServer s = new KodtestServer(args);
		s.start();
		s.join();
		s.shutdown();
	}
}
