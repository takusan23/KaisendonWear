package io.github.takusan23.kaisendon;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class LicenseActivity extends WearableActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        mTextView = (TextView) findViewById(R.id.licensetextView);

        String apach = "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License.";

        String mit = "MIT License\n" +
                "\n" +
                "Copyright (c) 2017 Toshihiro Yagi\n" +
                "\n" +
                "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                "of this software and associated documentation files (the \"Software\"), to deal\n" +
                "in the Software without restriction, including without limitation the rights\n" +
                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                "copies of the Software, and to permit persons to whom the Software is\n" +
                "furnished to do so, subject to the following conditions:\n" +
                "\n" +
                "The above copyright notice and this permission notice shall be included in all\n" +
                "copies or substantial portions of the Software.\n" +
                "\n" +
                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                "SOFTWARE.";

        String glide = "License for everything not in third_party and not otherwise marked:\n" +
                "\n" +
                "Copyright 2014 Google, Inc. All rights reserved.\n" +
                "\n" +
                "Redistribution and use in source and binary forms, with or without modification, are\n" +
                "permitted provided that the following conditions are met:\n" +
                "\n" +
                "   1. Redistributions of source code must retain the above copyright notice, this list of\n" +
                "         conditions and the following disclaimer.\n" +
                "\n" +
                "   2. Redistributions in binary form must reproduce the above copyright notice, this list\n" +
                "         of conditions and the following disclaimer in the documentation and/or other materials\n" +
                "         provided with the distribution.\n" +
                "\n" +
                "THIS SOFTWARE IS PROVIDED BY GOOGLE, INC. ``AS IS'' AND ANY EXPRESS OR IMPLIED\n" +
                "WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND\n" +
                "FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GOOGLE, INC. OR\n" +
                "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\n" +
                "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n" +
                "SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON\n" +
                "ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n" +
                "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF\n" +
                "ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" +
                "\n" +
                "The views and conclusions contained in the software and documentation are those of the\n" +
                "authors and should not be interpreted as representing official policies, either expressed\n" +
                "or implied, of Google, Inc.";

        //追加
        mTextView.append("square/okhttp\r\nhttps://github.com/square/okhttp\r\n\r\n" + apach);
        mTextView.append("\n\n----------\n\n");
        mTextView.append("sys1yagi/mastodon4j\r\nhttps://github.com/sys1yagi/mastodon4j\r\n\r\n" + mit);
        mTextView.append("\n\n----------\n\n");
        mTextView.append("bumptech/glide\r\nhttps://github.com/bumptech/glide\n\n" + glide);

        // Enables Always-on
        setAmbientEnabled();
    }
}
