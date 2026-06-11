window.layui = {
  use: function (mods, callback) {
    callback && callback();
  },
  layer: {
    msg: function (text) {
      window.toast(text);
    },
    confirm: function (text, callback) {
      if (window.confirm(text)) {
        callback && callback();
      }
    }
  }
};

