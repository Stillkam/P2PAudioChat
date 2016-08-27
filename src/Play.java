import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

/**
 * Created by Still on 16/5/24.
 */
public class Play{

    //定义录音格式
    AudioFormat af = null;
    //定义源数据行
    SourceDataLine sd = null;
    //定义字节数组输入输出流
    ByteArrayInputStream bais = null;
    //定义音频输入流
    AudioInputStream ais = null;

    public void play(byte []audioData) {
        //实例化字节数组输入输出流,讲音频数据放入
        bais = new ByteArrayInputStream(audioData);
        //实例化AudioFormat
        af=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000f, 16, 1, 2, 8000f, true);
        //实例化 AudioInputStream
        ais = new AudioInputStream(bais, af, audioData.length / af.getFrameSize());

        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, af);
            sd = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            //Opens the line with the specified format
            sd.open(af);
            sd.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                if (ais != null) {
                    ais.close();
                }
                if (bais != null) {
                    bais.close();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run(){

        byte bts[] = new byte[10240];
        try {
            int cnt;
            //读取数据到缓存数据
            while ((cnt = ais.read(bts, 0, bts.length)) != -1)
            {
                if (cnt > 0)
                {
                    //写入缓存数据
                    //将音频数据写入到混频器
                    sd.write(bts, 0, cnt);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pClose(){
        //关闭流
        sd.drain();
        sd.close();
    }
}
