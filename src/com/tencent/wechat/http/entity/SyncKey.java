package com.tencent.wechat.http.entity;

import java.io.Serializable;
import java.util.List;

public class SyncKey implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer Count;

    private List<SyncKeyValue> List;

    public class SyncKeyValue {
        private Integer Key;

        private Integer Val;

        public Integer getKey() {
            return Key;
        }

        public void setKey(Integer key) {
            Key = key;
        }

        public Integer getVal() {
            return Val;
        }

        public void setVal(Integer val) {
            Val = val;
        }
    }

    public Integer getCount() {
        return Count;
    }

    public void setCount(Integer count) {
        Count = count;
    }

    public List<SyncKeyValue> getList() {
        return List;
    }

    public void setList(List<SyncKeyValue> list) {
        List = list;
    }

}

