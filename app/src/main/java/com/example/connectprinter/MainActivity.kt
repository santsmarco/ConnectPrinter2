package com.example.connectprinter

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.button1_socketComIP_PORTA)
        val button2 = findViewById<Button>(R.id.button2_socket_apenas_porta)
        val button3 = findViewById<Button>(R.id.button3_print_framework)
        val button4 = findViewById<Button>(R.id.button4_escPosAndroid)
        val button5 = findViewById<Button>(R.id.button5_starIO)
        val button6 = findViewById<Button>(R.id.button6_zebra)
        val button7 = findViewById<Button>(R.id.button7_smb_cifs)
        val button8 = findViewById<Button>(R.id.button8_rawprinter)

        button1.setOnClickListener {
            printer1SocketIPePORTA("bematech", "Hello word")
        }

        button2.setOnClickListener {
            printer2SOCKETapenasPorta("bematech", "Hello Word", 9100)
        }

        button3.setOnClickListener {
            printer3_framework("Hello Word", this)
        }

        button4.setOnClickListener {
            //printer4ToEscPosPrinter("192.168.0.102", "Hello Word",9100)
        }

        button5.setOnClickListener {
            //printer5ToStarPrinter("192.168.0.102", "Hello Word",9100)
        }

        button6.setOnClickListener {
            //printer6ToZebraPrinter("192.168.0.102", "Hello Word",9100)
        }

        button7.setOnClickListener {
            //printer7SMBPrinter("192.168.0.102", "Hello Word")
        }

        button8.setOnClickListener {
            //printer8ToRawPrinter("192.168.0.102", 9100, "Hello Word")
        }
    }

    private fun printer1SocketIPePORTA(printerName: String, data: String) {
        val devices = mutableSetOf<String>()
        val net = NetworkInterface.getNetworkInterfaces()

        while (net.hasMoreElements()) {
            val face = net.nextElement() as NetworkInterface
            val addresses = face.inetAddresses

            while (addresses.hasMoreElements()) {
                val inetAddress = addresses.nextElement()

                if (!inetAddress.isLoopbackAddress && inetAddress.isSiteLocalAddress) {
                    devices.add(inetAddress.getHostAddress())
                }
            }
        }

        val printerIpAddress = devices.firstOrNull { it.contains(printerName) }

        if (printerIpAddress == null) {
            println("Impressora não encontrada na rede.")
            return
        }
    }

    private fun printer2SOCKETapenasPorta(printerName: String, data: String,porta: Int) {
        val devices = mutableSetOf<String>()
        val net = NetworkInterface.getNetworkInterfaces()

        while (net.hasMoreElements()) {
            val face = net.nextElement() as NetworkInterface
            val addresses = face.inetAddresses

            while (addresses.hasMoreElements()) {
                val inetAddress = addresses.nextElement()

                if (!inetAddress.isLoopbackAddress && inetAddress.isSiteLocalAddress && inetAddress is Inet4Address) {
                    val ip = inetAddress.hostAddress
                    val prefix = ip.substring(0, ip.lastIndexOf(".") + 1)

                    for (i in 0 until 255) {
                        val testIp = "$prefix$i"
                        devices.add(testIp)
                    }
                }
            }
        }

        val printerIpAddress = devices.firstOrNull{ ip ->
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, porta), 1000)
                    true
                }
            } catch (e: Exception) {
                false
            }
        } ?: run {
            println("Impressora não encontrada na rede.")
            return
        }

        Socket(printerIpAddress, 9100).use { socket ->
            socket.getOutputStream().write(data.toByteArray(Charsets.UTF_8))
        }
    }

    private fun printer3_framework(text: String, context: Context) {
        val jobName = "Print Job Name"
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                layoutResultCallback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal!!.isCanceled) {
                    layoutResultCallback?.onLayoutCancelled()
                    return
                }
                val builder = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                val info = builder.build()

                layoutResultCallback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                parcelFileDescriptor: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                writeResultCallback: WriteResultCallback?
            ) {
                try {
                    val input = ByteArrayInputStream(text.toByteArray())
                    val output = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } > 0) {
                        output.write(buffer, 0, bytesRead)
                    }
                    writeResultCallback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))

                } catch (e: Exception) {
                    writeResultCallback?.onWriteFailed(e.message)
                }
            }
        }

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            printManager.print(jobName, printAdapter, null)
        }catch (e: java.lang.Exception){
            Toast.makeText(this, "Erro ao imprimir", Toast.LENGTH_SHORT).show()
        }

    }

    //VALIDAR IMPLEMENTATION
    /*private fun printer4ToEscPosPrinter(printerIpAddress: String, textToPrint: String, porta: Int) {
        val printer = TcpConnection(printerIpAddress, porta)

        try {
            printer.open()
            val escPosPrinter = EscPosPrinter(printer)

            escPosPrinter.printFormattedText("{C}${textToPrint}{C}") // Imprime o texto em negrito

            escPosPrinter.feed(5) // Avança o papel 5 linhas

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            printer.close()
        }
    }*/

    //VALIDAR DEPENDENCIAS
    /*private fun printer5ToStarPrinter(printerIpAddress: String, textToPrint: String, porta: Int) {
        val channel = StarIoExt.createTcpPrintChannel(printerIpAddress, porta)

        try {
            channel.connect()

            channel.print(object : ILocalizeReceipts {
                override fun append(builder: ICommandBuilder): Boolean {
                    builder.appendAlignment(AlignmentPosition.Center)
                        .append(textToPrint)
                        .appendCutPaper()

                    return true
                }

                override fun getLanguage(): String {
                    return "en"
                }

                override fun getPaperSize(): PaperSize {
                    return PaperSize.ThreeInch
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            channel.disconnect()
        }
    }*/

    //VALIDAR IMPLEMENTATION
    /*private fun printer6ToZebraPrinter(printerIpAddress: String, textToPrint: String, porta: Int) {
        val printerConnection = TcpConnection(printerIpAddress, porta)

        try {
            printerConnection.open()
            val printer = ZebraPrinterFactory.getInstance(printerConnection)

            printerConnection.execute(byteArrayOf(0x1b, 0x21, 0x02)) // Habilita o modo de negrito
            printerConnection.write(textToPrint.toByteArray(Charset.forName("ISO-8859-1")))

            printerConnection.execute(byteArrayOf(0x1b, 0x40)) // Reinicia o padrão

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            printerConnection.close()
        }
    }*/

    //VALIDAR IMPLEMENTATION
    /*private fun printer7SMBPrinter(printerIpAddress: String, textToPrint: String) {
        val SMB_URL = "smb://${printerIpAddress}/BEMATECH"

        try {
            val smbFile = SmbFile(SMB_URL)

            if (smbFile.exists()) {
                val ostream = smbFile.outputStream
                ostream.write(textToPrint.toByteArray(Charset.forName("ISO-8859-1")))
                ostream.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    //VALIDAR IMPLEMENTATION
    /*private fun printer8ToRawPrinter(printerIpAddress: String, port: Int, textToPrint: String) {
        val printer = RawPrint.printer()
            .name("Printer")
            .host(printerIpAddress)
            .port(port)
            .raw()
            .open()

        printer.write(textToPrint.toByteArray(Charset.forName("ISO-8859-1")))

        printer.close()
    }*/



}
