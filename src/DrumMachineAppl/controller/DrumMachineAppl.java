package DrumMachineAppl.controller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.sound.midi.*;
import javax.swing.*;

public class DrumMachineAppl {
    
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequenser;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    private static final int N_BEATS = 16;
    
    String[] instrumentsName = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Symbal", "Hand Clap", 
        "High Tom", "High Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", 
        "High Agogo", "Open Hi Conga"};
    int[] instriments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
           new DrumMachineAppl().buildGui();
    }

    private void buildGui() {
        theFrame = new JFrame("Drum Machine");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout =  new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        checkboxList = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (String element : instrumentsName) {
            nameBox.add(new Label(element));
        }
        
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        theFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(instrumentsName.length, N_BEATS);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        int checkBoxesCount = instrumentsName.length * N_BEATS;
        for(int i = 0; i < checkBoxesCount; i++){
            JCheckBox c =  new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }
        
        setUpMidi();
        
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    private void setUpMidi() {
        try{
            sequenser = MidiSystem.getSequencer();
            sequenser.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequenser.setTempoInBPM(120);
        }catch(Exception ex){}
    }
    
    public void buildTrackAndStart() {
        int[] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        for(int i = 0; i < instriments.length; i++) {
            trackList = new int[instriments.length];
            int key = instriments[i];
            for(int j = 0; j < N_BEATS; j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (N_BEATS * i));
                if(jc.isSelected()){
                    trackList[j] = key;
                }else {
                    trackList[j] = 0;
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try{
            sequenser.setSequence(sequence);
            sequenser.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequenser.start();
            sequenser.setTempoInBPM(120);
        }catch(Exception ex){}
    }

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequenser.stop();
        }
    }

    private class MyUpTempoListener implements ActionListener { @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequenser.getTempoFactor();
            sequenser.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    private class MyDownTempoListener implements ActionListener {@Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequenser.getTempoFactor();
            sequenser.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }
    
    public void makeTracks(int[] list) {
        for(int i = 0; i < N_BEATS; i++){
            int key = list[i];
            if(key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    private MidiEvent makeEvent(int comd, int chan, int one, int two, int trick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, trick);
        } catch (Exception e) {}
        return event;
    }
}
