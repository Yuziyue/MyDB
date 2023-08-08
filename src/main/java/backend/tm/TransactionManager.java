package backend.tm;
import utils.Error;
import utils.Panic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import utils.*;
public interface TransactionManager {
    long begin();
    void commit(long xid);
    void abort(long xid);
    boolean isActive(long xid);
    boolean isCommitted(long xid);
    boolean isAborted(long xid);
    void close();


//创建一个 xid 文件并创建 TM
    public static TransactionManagerImpl create(String path) {
        File f = new File(path+TransactionManagerImpl.XID_SUFFIX);
        try {
            if(!f.createNewFile()) {
                Panic.panic(Error.FileExistsException);
            }
        } catch (Exception e) {
            Panic.panic(e);
        }
        if(!f.canRead() || !f.canWrite()) {
            Panic.panic(Error.FileCannotRWException);
        }

        FileChannel fc = null;

        /*
        RandomAccessFile既可以读取文件内容，也可以向文件输出数据。同时，RandomAccessFile支持“随机访问”的方式，程序快可以直接跳转到文件的任意地方来读写数据。
        由于RandomAccessFile可以自由访问文件的任意位置，所以如果需要访问文件的部分内容，而不是把文件从头读到尾，使用RandomAccessFile将是更好的选择。
        与OutputStream、Writer等输出流不同的是，RandomAccessFile允许自由定义文件记录指针，RandomAccessFile可以不从开始的地方开始输出，
        因此RandomAccessFile可以向已存在的文件后追加内容。如果程序需要向已存在的文件后追加内容，则应该使用RandomAccessFile。
        RandomAccessFile的方法虽然多，但它有一个最大的局限，就是只能读写文件，不能读写其他IO节点。
        RandomAccessFile的一个重要使用场景就是网络请求中的多线程下载及断点续传。
         */
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "rw");
            //通道获取
            fc = raf.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }

        // 写空XID文件头
        ByteBuffer buf = ByteBuffer.wrap(new byte[TransactionManagerImpl.LEN_XID_HEADER_LENGTH]);
        try {
            //从零创建 XID 文件时需要写一个空的 XID 文件头，即设置 xidCounter 为 0，
            // 否则后续在校验时会不合法：
            fc.position(0);
            fc.write(buf);
        } catch (IOException e) {
            Panic.panic(e);
        }
        return new TransactionManagerImpl(raf, fc);
    }
//从一个已有的 xid 文件来创建 TM
    public static TransactionManagerImpl open(String path) {
        File f = new File(path+TransactionManagerImpl.XID_SUFFIX);
        if(!f.exists()) {
            Panic.panic(Error.FileNotExistsException);
        }
        if(!f.canRead() || !f.canWrite()) {
            Panic.panic(Error.FileCannotRWException);
        }
        FileChannel fc = null;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "rw");//用来访问那些保存数据记录的文件
            fc = raf.getChannel();//返回与这个文件有关的唯一FileChannel对象
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }
        return new TransactionManagerImpl(raf, fc);
    }
}

