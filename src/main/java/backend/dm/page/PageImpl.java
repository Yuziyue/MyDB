package backend.dm.page;
import backend.dm.pageCache.PageCache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//本节主要内容就是 DM 模块向下对文件系统的抽象部分。DM 将文件系统抽象成页面，
// 每次对文件系统的读写都是以页面为单位的。同样，从文件系统读进来的数据也是以页面为单位进行缓存的。

//这里参考大部分数据库的设计，将默认数据页大小定为 8K。
// 如果想要提升向数据库写入大量数据情况下的性能的话，也可以适当增大这个值。
//注意这个页面是存储在内存中的，与已经持久化到磁盘的抽象页面有区别。

//DM模块是上层模块和文件系统之间的一个抽象层，向下从数据库直接读写文件（以Page的格式），向上提供数据的包装（以DataItem的格式）；
// 另外就是日志功能，发生错误时可以根据日志恢复。
//可以注意到，无论是向上还是向下，DM 都提供了一个缓存的功能，用内存操作来保证效率
//更详细的说
//1) 向下分页管理 DB 文件，并提供HashMap缓存，key是页号，value是page；
//2) 向上抽象 DB 文件为 DataItem 供上层模块使用，并提供HashMap缓存，key是uid,value是DataItem；
//3) 管理日志文件，保证在发生错误时可以根据日志进行恢复；
public class PageImpl implements Page {
    private int pageNumber;//pageNumber 是这个页面的页号，该页号从 1 开始
    private byte[] data;//data 就是这一页实际包含的字节数据
    private boolean dirty;//dirty 标志着这个页面是否是脏页面，在缓存驱逐的时候，
    // 脏页面需要被写回磁盘。
    private Lock lock;

    private PageCache pc;//PageCache的引用，用来方便在拿到 Page 的引用时
    // 可以快速对这个页面的缓存进行释放操作。

    public PageImpl(int pageNumber, byte[] data, PageCache pc) {
        this.pageNumber = pageNumber;
        this.data = data;
        this.pc = pc;
        lock = new ReentrantLock();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void release() {
        pc.release(this);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public byte[] getData() {
        return data;
    }

}

