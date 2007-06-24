package ch.rgw.tools;

/**
 * The <code>Base64</code> encoder/decoder class.
 *
 * See RFC 1521 for a description of the general algorithm.
 *
 */
public
class Base64
{
  public
  static
  byte []
  encode(byte [] arbIn)
  {
    int n = arbIn.length / 3;
    int m = arbIn.length % 3;
    int l = (n * 4) + (m > 0 ? 4 : 0);
    byte [] arbOut = new byte [l];
    int i = 0, j = 0;
    while (i < arbIn.length)
    {
      encode(arbIn, i, arbOut, j);
      i += 3;
      j += 4;
    }
    return arbOut;
  }

  static
  void
  encode(byte [] arbIn, int nOffsetIn, byte [] arbOut, int nOffsetOut)
  {
    int nBlock = 0;
    for (int i = 0; i < 3; i++)
    {
      int nIndex = nOffsetIn + i;
      byte nBits = (nIndex < arbIn.length ? arbIn[nIndex] : 0x00);
      nBlock += ((nBits & 0xFF) << (8 * (2 - i)));
    }
    for (int j = 0; j < 4; j++)
    {
      int nIndex = nOffsetOut + j;
      byte nBits = (byte)((nBlock >>> (6 * (3 - j))) & 0x3F);
      arbOut[nIndex] = encode(nBits);
    }
    if (arbIn.length - nOffsetIn == 1)
    {
      arbOut[nOffsetOut + 2] = (byte)'=';
      arbOut[nOffsetOut + 3] = (byte)'=';
    }
    if (arbIn.length - nOffsetIn == 2)
    {
      arbOut[nOffsetOut + 3] = (byte)'=';
    }
  }

  static
  byte
  encode(byte nBits)
  {
    if (nBits >= 0 && nBits <= 25)
      return (byte)('A' + nBits);
    else if (nBits >= 26 && nBits <= 51)
      return (byte)('a' + (nBits - 26));
    else if (nBits >= 52 && nBits <= 61)
      return (byte)('0' + (nBits - 52));
    else if (nBits == 62) return (byte)'+';
    else if (nBits == 63) return (byte)'/';
    return (byte)'?';
  }
}
