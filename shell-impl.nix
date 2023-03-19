{ pkgs }:

let
  lib = pkgs.lib;
  stdenv = pkgs.stdenv;
in
pkgs.hestia.shell.mkShell {
  name = "gdashboard-cli";

  buildInputs = [
    pkgs.s2n-tls
  ];
}
