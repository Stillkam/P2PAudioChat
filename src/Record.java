/**
 * Created by Still on 16/5/16.
 */

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Record{

    //定义录音格式
    AudioFormat af = null;
    //定义目标数据行
    TargetDataLine td = null;
    //定义字节数组输入输出流
    ByteArrayOutputStream baos = null;
    //定义byte数组用来保存音频数据
    byte audioData[];

    public Record(){

        try {
            //实例化AudioFormat
            af=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000f, 16, 1, 2, 8000f, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,af);
            td = (TargetDataLine)(AudioSystem.getLine(info));
            //Opens the line with the specified format
            td.open(af);
            //允许某一数据行执行数据 I/O
            td.start();



        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            return;
        }

    }

    public void run(){
        byte bts[] = new byte[10240];
        //实例化字节数组输入输出流
        baos = new ByteArrayOutputStream();
        try {

            //从数据行的输入缓冲区读取音频数据。
            //要读取bts.length长度的字节,cnt 是实际读取的字节数
            int cnt = td.read(bts, 0, bts.length);
            if(cnt > 0)
            {
                //讲音频数据写入baos
                baos.write(bts, 0, cnt);
                //讲音频数据保存为byte类型
                audioData = baos.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public byte[] getAudioData(){
        //获取音频数据
        return audioData;
    }
    public void rClose(){
        try {
            //关闭打开的字节数组流
            if(baos != null)
            {
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //关闭流
            td.drain();
            td.close();
        }
    }

}
