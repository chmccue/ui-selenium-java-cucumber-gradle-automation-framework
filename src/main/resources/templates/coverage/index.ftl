<!DOCTYPE html>
<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Code Coverage for ${fullpath}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${libspath}/bootstrap.min.css" rel="stylesheet">
    <link href="${libspath}/style.css" rel="stylesheet">
    <!--[if lt IE 9]>
    <script src="${libspath}/html5shiv.min.js"></script>
    <script src="${libspath}/respond.min.js"></script>
    <![endif]-->
    <style>@media print {#ghostery-purple-box {display:none !important}}</style></head>
<body>
<header>
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <ol class="breadcrumb">
                    <li class="active">${fullpath}</li>
                    <li>(<a href="javascript:window.history.back();">Back</a>)</li>
                    <li>(<a href="${libspath}/../index.html">Home</a>)</li>
                </ol>
            </div>
        </div>
    </div>
</header>
<div class="container">
    <table class="table table-bordered">
        <thead>
        <tr>
            <td>&nbsp;</td>
            <td colspan="6"><div align="center"><strong>Code Coverage</strong></div></td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td colspan="3"><div align="center"><strong>Files of Project</strong></div></td>
            <td colspan="3"><div align="center"><strong>Lines of Code</strong></div></td>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="${subtotal.rowColor} file">${subtotal.path}</td>
            <td class="${subtotal.filesColor} big">
                <div class="progress">
                    <div class="progress-bar progress-bar-${subtotal.filesColor}" role="progressbar" aria-valuenow="${subtotal.filesCoveredRate}" aria-valuemin="0" aria-valuemax="100" style="width: ${subtotal.filesCoveredRate}%">
                        <span class="sr-only">${subtotal.filesCoveredRate}% covered (${subtotal.filesColor})</span>
                    </div>
                </div>
            </td>
            <td class="${subtotal.filesColor} small"><div align="right">${subtotal.filesCoveredRate}%</div></td>
            <td class="${subtotal.filesColor} small"><div align="right">${subtotal.coveredFiles}&nbsp;/&nbsp;${subtotal.totalFiles}</div></td>
            <td class="${subtotal.linesColor} big">
                <div class="progress">
                    <div class="progress-bar progress-bar-${subtotal.linesColor}" role="progressbar" aria-valuenow="0.00" aria-valuemin="0" aria-valuemax="100" style="width: ${subtotal.linesCoveredRate}%">
                        <span class="sr-only">${subtotal.linesCoveredRate}% covered (${subtotal.linesColor})</span>
                    </div>
                </div>
            </td>
            <td class="${subtotal.linesColor} small"><div align="right">${subtotal.linesCoveredRate}%</div></td>
            <td class="${subtotal.linesColor} small"><div align="right">${subtotal.coveredLines}&nbsp;/&nbsp;${subtotal.totalLines}</div></td>
        </tr>
        </tr>

        <#list items as item>
            <tr>
                <td class="${item.rowColor}"><span class="glyphicon glyphicon-folder-open"></span> <a href="${item.relativeUrl}">${item.path}</a></td>
                <td class="${item.filesColor} big">
                    <div class="progress">
                        <div class="progress-bar progress-bar-${item.filesColor}" role="progressbar" aria-valuenow="${item.filesCoveredRate}" aria-valuemin="0" aria-valuemax="100" style="width: ${item.filesCoveredRate}%">
                            <span class="sr-only">${item.filesCoveredRate}% covered (${item.filesColor})</span>
                        </div>
                    </div>
                </td>
                <td class="${item.filesColor} small"><div align="right">${item.filesCoveredRate}%</div></td>
                <td class="${item.filesColor} small"><div align="right">${item.coveredFiles}&nbsp;/&nbsp;${item.totalFiles}</div></td>
                <td class="${item.linesColor} big">
                    <div class="progress">
                        <div class="progress-bar progress-bar-${item.linesColor}" role="progressbar" aria-valuenow="0.00" aria-valuemin="0" aria-valuemax="100" style="width: ${item.linesCoveredRate}%">
                            <span class="sr-only">${item.linesCoveredRate}% covered (${item.linesColor})</span>
                        </div>
                    </div>
                </td>
                <td class="${item.linesColor} small"><div align="right">${item.linesCoveredRate}%</div></td>
                <td class="${item.linesColor} small"><div align="right">${item.coveredLines}&nbsp;/&nbsp;${item.totalLines}</div></td>
            </tr>
        </#list>


        </tbody>
    </table>
    <footer>
        <hr>
        <h4>Legend</h4>
        <p>
            <span class="danger"><strong>Low</strong>: 0% to 50%</span>
            <span class="warning"><strong>Medium</strong>: 50% to 90%</span>
            <span class="success"><strong>High</strong>: 90% to 100%</span>
        </p>
        <p>
            <small>Coverage collected with PHP xdebug and summary report generated by Company UAT at ${now}</small>
        </p>
    </footer>
</div>
<script src="${libspath}/jquery.min.js" type="text/javascript"></script>
<script src="${libspath}/bootstrap.min.js" type="text/javascript"></script>
<script src="${libspath}/holder.min.js" type="text/javascript"></script>
