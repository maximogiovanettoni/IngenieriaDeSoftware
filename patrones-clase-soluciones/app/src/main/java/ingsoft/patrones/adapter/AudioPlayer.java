package ingsoft.patrones.adapter;

public class AudioPlayer implements MediaPlayer {
  private AdvancedMediaPlayer advancedMediaPlayer;

  @Override
  public String play(String audioType, String fileName) {
    if (audioType.equals("mp3")) {
      return "Playing mp3 file. Name: " + fileName;
    } else if (audioType.equals("wav")) {
      advancedMediaPlayer = new WavPlayer();
      return advancedMediaPlayer.playWav(fileName);
    } else if (audioType.equals("ogg")) {
      advancedMediaPlayer = new OggPlayer();
      return advancedMediaPlayer.playOgg(fileName);
    } else {
      return "Invalid media. " + audioType + " format not supported";
    }
  }
}
