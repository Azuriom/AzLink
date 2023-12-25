package com.azuriom.azlink.common;

import com.azuriom.azlink.common.utils.Hash;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashTest {

    @ParameterizedTest
    @MethodSource("hashTestData")
    void testSha256(String[] data) {
        assertEquals(data[1], Hash.SHA_256.hash(data[0]));
    }

    @ParameterizedTest
    @MethodSource("hashTestData")
    void testSha384(String[] data) {
        assertEquals(data[2], Hash.SHA_384.hash(data[0]));
    }

    @ParameterizedTest
    @MethodSource("hashTestData")
    void testSha512(String[] data) {
        assertEquals(data[3], Hash.SHA_512.hash(data[0]));
    }

    private static Stream<Arguments> hashTestData() {
        String[] test1 = {
                "H5vBfLcF3vqaCo8",
                "e936363862ca96e0866afaa51ce622959a626938b1328a6a03921941989922fb",
                "ad7e3d46584ebcfff0c420e0b7e40b1c612f0f46d94054af2dcbf0f65a148ec47e56461ec93073f6b35c24aafd8906dc",
                "754d79ec450e8f1090aba4b0c25e9a0602d351561cb6f39c902ad3a9e779dbaf2d7ae194dde3d35492530f77566fc90d0137027f3fdf8fa560c8d19ed73767ec",
        };
        String[] test2 = {
                "YCJLMo7uX5t7WxG",
                "35d729f6ab4c9abe9409bbdca2896eba1bc4608dab2e0f80fece72a67002866b",
                "1e49c84bc3b4d827fafef3fe7ef34966d5884ebe1bf814d6ec71d05cb530889fe196ed8a184c82377b566c8bfd95ebdd",
                "c4c940c69bf0cbd24057d409be393373e26593d5b0ac8117c49c929a17131defe321dea2589664e75c438e0bf0635074a91ee3f4fbb3d3e3b211f9771587fdee",
        };
        String[] test3 = {
                "g3kmfbpwfHdDFQL",
                "cfa211834f280d238965a6d4afa15c8851b3286ee0cec146dd6a598e389568e3",
                "96b714d99c039829328275f3b90fa5abe304e4d78ed8114292ab0d16e9c1e636c57d763aae11f8490b2cd8216c5b64c2",
                "370ca5636ecbabe0f992dae196fe79cadeddea736a712a3cca5c9417bdffc252e26636e6e1d0a08c35ee10f28f5c88fa165935e0d5bc8ce480a14ee6b8950f61",
        };

        return Stream.of(test1, test2, test3).map(s -> Arguments.of((Object) s));
    }
}
