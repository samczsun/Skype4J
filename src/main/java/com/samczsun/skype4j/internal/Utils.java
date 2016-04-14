/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.chat.ChatImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

    public static JsonObject parseJsonObject(InputStream inputStream) throws IOException {
        return parseJsonValue(inputStream).asObject();
    }

    public static JsonArray parseJsonArray(InputStream inputStream) throws IOException {
        return parseJsonValue(inputStream).asArray();
    }

    public static JsonValue parseJsonValue(InputStream inputStream) throws IOException {
        JsonValue jsonValue;
        try (InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8")) {
            jsonValue = JsonValue.readFrom(reader);
        }
        return jsonValue;
    }

    public static String uploadImage(byte[] image, ImageType uploadType, ChatImpl chat) throws ConnectionException {
        return upload(image, uploadType, null, chat);
    }

    public static String upload(byte[] data, ImageType type, JsonObject extra, ChatImpl chat) throws ConnectionException {
        JsonObject obj = new JsonObject();
        obj.add("type", type.mime);
        obj.add("permissions", new JsonObject().add(chat.getIdentity(), new JsonArray().add("read")));
        if (extra != null) extra.forEach(m -> obj.add(m.getName(), m.getValue()));

        JsonObject response = Endpoints.OBJECTS
                .open(chat.getClient())
                .as(JsonObject.class)
                .expect(201, "While uploading data")
                .post(obj);

        String id = response.get("id").asString();

        Endpoints.UPLOAD_IMAGE
                .open(chat.getClient(), id, type.endpoint)
                .header("Content-Type", "multipart/form-data")
                .expect(201, "While uploading data")
                .connect("PUT", data);

        Endpoints.EndpointConnection<JsonObject> econn = Endpoints.IMG_STATUS
                .open(chat.getClient(), id, type.id)
                .as(JsonObject.class)
                .expect(200, "While getting upload status");
        while (true) {
            JsonObject status = econn.get();
            if (status.get("view_state").asString().equals("ready")) {
                break;
            }
        }
        return id;
    }

    public static String getString(JsonObject object, String key) {
        return object.get(key) == null ? null : object.get(key).isNull() ? null : object.get(key).asString();
    }

    public static String coerceToString(JsonValue value) {
        return value.isString() ? value.asString() : value.toString();
    }

    public enum ImageType {
        IMGT1("pish/image", "imgpsh", "imgt1"),
        AVATAR("avatar/group", "avatar", "avatar_fullsize"), //Also has "avatar"
        FILE("sharing/file", "original", "thumbnail");

        private String mime;
        private String endpoint;
        private String id;

        ImageType(String mime, String endpoint, String id) {
            this.mime = mime;
            this.endpoint = endpoint;
            this.id = id;
        }
    }

    public static <T> Stream<T> asStream(Iterable<T> sourceIterable) {
        return asStream(sourceIterable.iterator());
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static void sneakyThrow(Throwable ex) {
        Utils.<RuntimeException>sneakyThrowInner(ex);
    }

    private static <T extends Throwable> T sneakyThrowInner(Throwable ex) throws T {
        throw (T) ex;
    }

    private static String rightPad(String in, int len, String pad) {
        for (int i = 0; i < len; i++) {
            in += pad;
        }
        return in;
    }

    private static String o(String e) {
        try {
            byte[] n = e.getBytes(StandardCharsets.UTF_8);
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            byte[] r = mDigest.digest(n);
            return DatatypeConverter.printHexBinary(r);
        } catch (NoSuchAlgorithmException e1) {
            throw new RuntimeException(e1);
        }
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[4096];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    public static String makeValidBase64(String input) {
        while (input.length() % 4 != 0) input += "=";
        return input;
    }

    private static final String FORMAT = "appId=%s; time=%s; lockAndKeyResponse=%s";

    private static String generateTime() {
        long ms = System.currentTimeMillis();
        return String.valueOf(Math.round(ms / 1000.0));
    }

    public static String generateChallengeHeader() {
        String time = generateTime();
        String appid = "msmsgs@msnmsgr.com";
        String secret = "Q1P7W2E4J9R8U3S5";
        return String.format(FORMAT, appid, time, generateChallenge(time, appid, secret));
    }

    public static String generateChallenge(String t, String n, String r) {
        String s = t + n;
        String f = s;
        int l = 8 - f.length() % 8;
        if (l != 8) {
            f = rightPad(f, l, "0");
        }
        int c = f.length() / 4;
        long[] h = new long[c];
        for (int p = 0, d = 0; p < c; p++) {
            h[p] = 0;
            h[p] = h[p] + f.charAt(d++) * 1L;
            h[p] = h[p] + f.charAt(d++) * 256L;
            h[p] = h[p] + f.charAt(d++) * 65536L;
            h[p] = h[p] + f.charAt(d++) * 16777216L;
        }
        long[] v = new long[4];
        String m = o(t + r);
        for (int p = 0, d = 0; p < v.length; p++) {
            v[p] = 0;
            v[p] += Integer.parseInt(m.substring(d, d + 2), 16) * 1L;
            d += 2;
            v[p] += Integer.parseInt(m.substring(d, d + 2), 16) * 256L;
            d += 2;
            v[p] += Integer.parseInt(m.substring(d, d + 2), 16) * 65536L;
            d += 2;
            v[p] += Integer.parseInt(m.substring(d, d + 2), 16) * 16777216L;
            d += 2;
        }
        long[] g = new long[2];
        _cS64_C(h, v, g);
        long y = u(v[0], g[0]);
        long b = u(v[1], g[1]);
        long w = u(v[2], g[0]);
        long E = u(v[3], g[1]);
        // Reverse parity
        y = Long.reverseBytes(y) >>> 32;
        b = Long.reverseBytes(b) >>> 32;
        w = Long.reverseBytes(w) >>> 32;
        E = Long.reverseBytes(E) >>> 32;
        return Long.toHexString(y) + Long.toHexString(b) + Long.toHexString(w) + Long.toHexString(E);
    }
    /* function(t, n, r) {
        var s = t + n,
            f = s,
            l = 8 - f.length % 8;
        l !== 8 && (f = a(f, f.length + l, "0"));
        var c = f.length / 4,
            h = [],
            p, d;
        for (p = 0, d = 0; p < c; p++) h.splice(p, 0, 0), h[p] = h[p] + f.charCodeAt(d++) * 1, h[p] = h[p] + f.charCodeAt(d++) * 256, h[p] = h[p] + f.charCodeAt(d++) * 65536, h[p] = h[p] + f.charCodeAt(d++) * 16777216;
        var v = new Array(4),
            m = o(t + r);
        for (p = 0, d = 0; p < v.length; p++) v[p] = 0, v[p] += i.parseHexInt(m.substr(d, 2)) * 1, d += 2, v[p] += i.parseHexInt(m.substr(d, 2)) * 256, d += 2, v[p] += i.parseHexInt(m.substr(d, 2)) * 65536, d += 2, v[p] += i.parseHexInt(m.substr(d, 2)) * 16777216, d += 2;
        var g = new Array(2);
        this._cS64_C(h, v, g);
        var y = u(v[0], g[0]),
            b = u(v[1], g[1]),
            w = u(v[2], g[0]),
            E = u(v[3], g[1]);
        return this._int32ToHexString(y) + this._int32ToHexString(b) + this._int32ToHexString(w) + this._int32ToHexString(E)
    }*/

    private static void _cS64_C(long[] t, long[] n, long[] i) {
        long s = 2147483647;
        if (t.length < 2 || (t.length & 1) == 1) {
            return;
        }
        long o = n[0] & s;
        long u = n[1] & s;
        long a = n[2] & s;
        long f = n[3] & s;
        long l = 242854337;

        BigInteger c = new BigInteger(String.valueOf(o), 10);
        BigInteger h = new BigInteger(String.valueOf(u), 10);
        BigInteger p = new BigInteger(String.valueOf(a), 10);
        BigInteger d = new BigInteger(String.valueOf(f), 10);
        BigInteger v = new BigInteger(String.valueOf(l), 10);
        int m = 0;
        BigInteger g = new BigInteger(String.valueOf(s), 10);
        BigInteger y = new BigInteger("0", 10);
        BigInteger b = new BigInteger("0", 10);
        BigInteger w = new BigInteger("0", 10);
        for (int E = 0; E < t.length / 2; E++) {
            y = new BigInteger(String.valueOf(t[m++]), 10);
            y = y.multiply(v);
            y = y.mod(g);
            b = b.add(y);
            b = b.multiply(c);
            b = b.add(h);
            b = b.mod(g);
            w = w.add(b);
            b = b.add(new BigInteger(String.valueOf(t[m++]), 10));
            b = b.multiply(p);
            b = b.add(d);
            b = b.mod(g);
            w = w.add(b);
        }
        b = b.add(h);
        b = b.mod(g);
        w = w.add(d);
        w = w.mod(g);
        i[0] = Long.parseLong(b.toString(), 10);
        i[1] = Long.parseLong(w.toString(), 10);
    }

    /* function _cS64_C(t, n, i) {
        var s = 2147483647;
        if (t.length < 2 || (t.length & 1) == = 1) {
            return false;
        }
        var o = n[0] & s;
        var u = n[1] & s;
        var a = n[2] & s;
        var f = n[3] & s;
        var l = 242854337;
        var c = r.parseDecInt(r.decRadix, o.toString());
        var h = r.parseDecInt(r.decRadix, u.toString());
        var p = r.parseDecInt(r.decRadix, a.toString());
        var d = r.parseDecInt(r.decRadix, f.toString());
        var v = r.parseDecInt(r.decRadix, l.toString());
        var m = 0;
        var g = r.parseDecInt(r.decRadix, s.toString());
        var y = r.parseDecInt(r.decRadix, "0");
        var b = r.parseDecInt(r.decRadix, "0");
        var w = r.parseDecInt(r.decRadix, "0");
        var E = 0;
        for (; E < t.length / 2; E++) {
            y = r.parseDecInt(r.decRadix, t[m++].toString());
            y.multiply(v);
            y.modulus(g);
            b.add(y);
            b.multiply(c);
            b.add(h);
            b.modulus(g);
            w.add(b);
            b.add(r.parseDecInt(r.decRadix, t[m++].toString()));
            b.multiply(p);
            b.add(d);
            b.modulus(g);
            w.add(b);
        }
        return b.add(h),
        b.modulus(g), w.add(d), w.modulus(g), i[0] = parseInt(b.toString(), 10), i[1] = parseInt(w.toString(), 10), true;
    } */

    private static long u(long e, long t) {
        String r = Long.toBinaryString(e);
        String i = Long.toBinaryString(t);
        StringBuilder s = new StringBuilder();
        StringBuilder o = new StringBuilder();
        int u = Math.abs(r.length() - i.length());
        for (int a = 0; a < u; a++) {
            o.append("0");
        }
        if (r.length() < i.length()) {
            o.append(r);
            r = o.toString();
        } else {
            if (i.length() < r.length()) {
                o.append(i);
                i = o.toString();
            }
        }
        for (int a = 0; a < r.length(); a++) {
            s.append(r.charAt(a) == i.charAt(a) ? "0" : "1");
        }
        return Long.parseLong(s.toString(), 2);
    }

    /* function u(e, t) {
        var r = e.toString(2);
        var i = t.toString(2);
        var s = new n.StringBuilder;
        var o = new n.StringBuilder;
        var u = Math.abs(r.length - i.length);
        var a;
        a = 0;
        for (; a < u; a++) {
            o.append("0");
        }
        if (r.length < i.length) {
            o.append(r);
            r = o.toString();
        } else {
            if (i.length < r.length) {
                o.append(i);
                i = o.toString();
            }
        }
        a = 0;
        for (; a < r.length; a++) {
            s.append(r.charAt(a) == = i.charAt(a) ? "0" : "1");
        }
        return parseInt(s.toString(), 2);
    }*/
}
